package com.duhao.vertx.common.handler;

import com.duhao.vertx.common.model.dto.result.SingleResult;
import com.duhao.vertx.common.verticle.ExceptionHandler;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/7
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public SingleResult<String> handle(Throwable throwable, HttpServerRequest request) {
        request.response().setStatusCode(500);
        return SingleResult.fail(500, "Internal error", "Internal error");
    }
}