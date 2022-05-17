package com.duhao.vertx.common.verticle;

import javax.annotation.Resource;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

/**
 * @author duhao
 */
@Slf4j
public class HttpVerticle extends AbstractVerticle {

    private HttpServer httpServer;

    @Resource
    private DispatcherHandler dispatcherHandler;

    @Resource
    private HttpServerProperties httpServerProperties;


    @Override
    public void start(Promise<Void> startPromise) {
        this.httpServer = vertx.createHttpServer();

        initHttpServer();

        doListen(startPromise);
    }

    private void initHttpServer() {
        setExceptionHandler();
        setDispatcherHandler();
    }

    private void setDispatcherHandler() {
        Router router = Router.router(vertx);
        router.route().handler(dispatcherHandler);
        httpServer.requestHandler(router);
    }

    private void setExceptionHandler() {
        // 用于处理socket连接异常
        httpServer.exceptionHandler(throwable -> log.error("Socket connection error.", throwable));
    }

    private void doListen(Promise<Void> startPromise) {
        int port = httpServerProperties.getPort();
        httpServer.listen(port, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                log.info("Vertx HTTP Server listening on port {}", port);
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}