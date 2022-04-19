package com.duhao.vertx.common.verticle;

import io.vertx.core.http.HttpServerRequest;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/7
 */
public interface RequestHandler {

  Object handle(HttpServerRequest request);
}