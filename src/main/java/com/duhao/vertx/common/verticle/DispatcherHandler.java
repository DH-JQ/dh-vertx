package com.duhao.vertx.common.verticle;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;

import com.duhao.vertx.common.annotation.VertxExceptionHandler;
import com.duhao.vertx.common.annotation.VertxRequestHandler;
import com.duhao.vertx.common.handler.DefaultExceptionHandler;
import com.duhao.vertx.common.handler.NotFoundRequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/3/31
 */
@Slf4j
public class DispatcherHandler implements Handler<HttpServerRequest>, ApplicationRunner {

  private static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultExceptionHandler();
  private static final RequestHandler NOT_FOUND_REQUEST_HANDLER = new NotFoundRequestHandler();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * 业务异常处理器
   */
  private final Map<Class<?>, ExceptionHandler> exceptionHandlerMapping = new ConcurrentHashMap<>();

  /**
   * 请求处理器
   */
  private final Map<String, RequestHandler> requestHandlerMapping = new ConcurrentHashMap<>();

  @Resource
  private Vertx vertx;

  @Resource
  private ApplicationContext applicationContext;

  @Resource
  private HttpServerProperties httpServerProperties;

  @Override
  public void handle(HttpServerRequest request) {

    String path = request.path();

    RequestHandler requestHandler = getRequestHandler(path);

    try {
      vertx.executeBlocking(blockingCodeHandler(request, requestHandler), false, asyncResultHandler(request));
    } catch (Throwable t) {
      processThrowable(t, request);
    }
  }

  private Handler<Promise<Object>> blockingCodeHandler(HttpServerRequest request,
                                                  RequestHandler requestHandler) {
    return promise -> {
      Object result = requestHandler.handle(request);
      promise.complete(result);
    };
  }

  private Handler<AsyncResult<Object>> asyncResultHandler(HttpServerRequest request) {
    return asyncResult -> {
      log.info("result: {}", asyncResult);

      if (asyncResult.succeeded()) {
        processResult(asyncResult.result(), request);
      }

      if (asyncResult.failed()) {
        processThrowable(asyncResult.cause(), request);
      }
    };
  }

  private void processResult(Object result, HttpServerRequest request) {
    HttpServerResponse response = request.response();
    if (response.closed()) {
      return;
    }

    int statusCode = response.getStatusCode();

    if (statusCode <= 0) {
      statusCode = 200;
    }

    String data;
    try {
      data = OBJECT_MAPPER.writeValueAsString(result);
    } catch (JsonProcessingException e) {
      log.error("result serialize failed", e);
      data = null;
      statusCode = 500;
    }

    if (response.closed()) {
      return;
    }
    response.setStatusCode(statusCode)
      .putHeader("content-type", "application/json")
      .end(data);
  }

  private RequestHandler getRequestHandler(String uri) {
    if (uri == null) {
      return NOT_FOUND_REQUEST_HANDLER;
    }

    RequestHandler requestHandler;
    if (HttpServerProperties.DEFAULT_HANDLER_PATH.equals(httpServerProperties.getHandlerPath())) {
      requestHandler = requestHandlerMapping.get(uri);
    } else {
      String relativePath = uri.substring(httpServerProperties.getHandlerPath().length());
      requestHandler = requestHandlerMapping.get(relativePath);
    }
    if (Objects.isNull(requestHandler)) {
      return NOT_FOUND_REQUEST_HANDLER;
    }
    return requestHandler;
  }

  private void processThrowable(Throwable t, HttpServerRequest request) {
    Class<? extends Throwable> clz = t.getClass();
    // 递归处理异常
    ExceptionHandler exceptionHandler = getExceptionHandler(clz);
    Object result = exceptionHandler.handle(t, request);

    processResult(result, request);
  }

  private ExceptionHandler getExceptionHandler(Class<?> clz) {
    if (clz == null) {
      return DEFAULT_EXCEPTION_HANDLER;
    }
    ExceptionHandler handler = exceptionHandlerMapping.get(clz);
    if (Objects.nonNull(handler)) {
      return handler;
    }

    Class<?> superclass = clz.getSuperclass();
    return getExceptionHandler(superclass);
  }

  private void setRequestHandlerMapping() {
    Map<String, RequestHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestHandler.class);
    for (Map.Entry<String, RequestHandler> beanEntry : beans.entrySet()) {
      RequestHandler handler = beanEntry.getValue();

      if (handler.getClass().isAnnotationPresent(VertxRequestHandler.class)) {
        VertxRequestHandler annotation = handler.getClass().getAnnotation(VertxRequestHandler.class);
        String[] uri = annotation.path();
        for (String s : uri) {
          RequestHandler ret = requestHandlerMapping.putIfAbsent(s, handler);

          if (ret != null && handler != ret) {
            throw new IllegalArgumentException(String.format("Duplicate uri: %s and %s", handler.getClass().getName(),
              ret.getClass().getName()));
          }
        }
      }
    }
    log.debug("add request controller to cache: {}", requestHandlerMapping);
  }

  private void setExceptionHandlerMapping() {
    Map<String, ExceptionHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ExceptionHandler.class);
    for (Map.Entry<String, ExceptionHandler> beanEntry : beans.entrySet()) {
      ExceptionHandler handler = beanEntry.getValue();

      if (handler.getClass().isAnnotationPresent(VertxExceptionHandler.class)) {
        VertxExceptionHandler annotation = handler.getClass().getAnnotation(VertxExceptionHandler.class);
        Class<? extends Throwable>[] exceptions = annotation.exception();
        for (Class<?> clz : exceptions) {
          ExceptionHandler ret = exceptionHandlerMapping.putIfAbsent(clz, handler);

          if (ret != null && handler != ret) {
            throw new IllegalArgumentException(String.format("Duplicate uri: %clz and %clz",
              handler.getClass().getName(), ret.getClass().getName()));
          }
        }
      }
    }
    log.debug("add exception handler to cache: {}", exceptionHandlerMapping);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    setRequestHandlerMapping();
    setExceptionHandlerMapping();
  }
}