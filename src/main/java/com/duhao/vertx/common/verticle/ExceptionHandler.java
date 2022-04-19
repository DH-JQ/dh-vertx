package com.duhao.vertx.common.verticle;

import io.vertx.core.http.HttpServerRequest;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/7
 */
public interface ExceptionHandler {

  /**
   *
   * @param t
   * @param request
   */
  Object handle(Throwable t, HttpServerRequest request);
}