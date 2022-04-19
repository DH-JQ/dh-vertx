package com.duhao.vertx.common.handler;

import com.duhao.vertx.common.model.dto.result.Result;
import com.duhao.vertx.common.verticle.RequestHandler;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/8
 */
public class NotFoundRequestHandler implements RequestHandler {
    @Override
    public Result<String> handle(HttpServerRequest request) {
        return Result.fail(404, "Not found", request.uri());
    }
}