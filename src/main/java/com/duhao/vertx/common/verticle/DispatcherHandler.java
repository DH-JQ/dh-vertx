package com.duhao.vertx.common.verticle;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class DispatcherHandler implements Handler<RoutingContext>, ApplicationRunner {

    private static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultExceptionHandler();
    private static final RequestHandler NOT_FOUND_REQUEST_HANDLER = new NotFoundRequestHandler();
    private static final String TRACE_ID = "Trace-Id";
    /**
     * 业务异常处理器
     */
    private final Map<Class<?>, ExceptionHandler> exceptionHandlerMapping = new ConcurrentHashMap<>();
    /**
     * 请求处理器
     */
    private final Map<String, RequestHandler> requestHandlerMapping = new ConcurrentHashMap<>();
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private Vertx vertx;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private HttpServerProperties httpServerProperties;

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        String traceId = getTraceId(request);

        try {
            MDC.put(TRACE_ID, traceId);
            logRequest(request);
            String path = getPath(request);
            RequestHandler requestHandler = getRequestHandler(path);
            vertx.executeBlocking(blockingCodeHandler(request, requestHandler), false, asyncResultHandler(request));
        } catch (Throwable t) {
            processThrowable(t, request);
        } finally {
            MDC.clear();
        }
    }

    private void logRequest(HttpServerRequest request) {
        log.trace(request.path());
        log.trace(request.headers().toString());
    }

    private String getPath(HttpServerRequest request) {
        return request.path();
    }

    private String getTraceId(HttpServerRequest request) {
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            request.headers().add(TRACE_ID, traceId);
        }
        return traceId;
    }

    private Handler<Promise<Object>> blockingCodeHandler(HttpServerRequest request,
                                                         RequestHandler requestHandler) {
        return promise -> {
            try {
                MDC.put(TRACE_ID, request.getHeader(TRACE_ID));
                Object result = requestHandler.handle(request);
                promise.complete(result);
            } finally {
                MDC.clear();
            }
        };
    }

    private Handler<AsyncResult<Object>> asyncResultHandler(HttpServerRequest request) {
        return asyncResult -> {
            try {
                MDC.put(TRACE_ID, request.getHeader(TRACE_ID));
                if (asyncResult.succeeded()) {
                    processResult(asyncResult.result(), request);
                }
                if (asyncResult.failed()) {
                    processThrowable(asyncResult.cause(), request);
                }
            } finally {
                MDC.clear();
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
            data = objectMapper.writeValueAsString(result);
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
        logResponse(data);
    }

    private void logResponse(String data) {
        log.info("response:{}", data);
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
        Map<String, RequestHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
            RequestHandler.class);
        for (Map.Entry<String, RequestHandler> beanEntry : beans.entrySet()) {
            RequestHandler handler = beanEntry.getValue();

            if (handler.getClass().isAnnotationPresent(VertxRequestHandler.class)) {
                VertxRequestHandler annotation = handler.getClass().getAnnotation(VertxRequestHandler.class);
                String[] uri = annotation.path();
                for (String s : uri) {
                    RequestHandler ret = requestHandlerMapping.putIfAbsent(s, handler);

                    if (ret != null && handler != ret) {
                        throw new IllegalArgumentException(String.format(
                            "Duplicate uri: uri=%s, handler=%s, existed=%s",
                            s, handler.getClass().getName(), ret.getClass().getName()));
                    }
                }
            }
        }
        log.debug("add request controller to cache: {}", requestHandlerMapping);
    }

    private void setExceptionHandlerMapping() {
        Map<String, ExceptionHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
            ExceptionHandler.class);
        for (Map.Entry<String, ExceptionHandler> beanEntry : beans.entrySet()) {
            ExceptionHandler handler = beanEntry.getValue();

            if (handler.getClass().isAnnotationPresent(VertxExceptionHandler.class)) {
                VertxExceptionHandler annotation = handler.getClass().getAnnotation(VertxExceptionHandler.class);
                Class<? extends Throwable>[] exceptions = annotation.exception();
                for (Class<?> clz : exceptions) {
                    ExceptionHandler ret = exceptionHandlerMapping.putIfAbsent(clz, handler);

                    if (ret != null && handler != ret) {
                        throw new IllegalArgumentException(String.format(
                            "Duplicate exception handler:  clz=%s, handler=%s, existed=%s",
                            clz.getName(), handler.getClass().getName(), ret.getClass().getName()));
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