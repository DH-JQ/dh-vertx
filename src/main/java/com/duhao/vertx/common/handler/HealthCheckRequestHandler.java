package com.duhao.vertx.common.handler;

import java.time.Instant;

import com.duhao.vertx.common.annotation.VertxRequestHandler;
import com.duhao.vertx.common.model.dto.HealthResp;
import com.duhao.vertx.common.model.dto.result.SingleResult;
import com.duhao.vertx.common.verticle.RequestHandler;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/7
 */
@VertxRequestHandler(path = "/health")
public class HealthCheckRequestHandler implements RequestHandler {

  @Override
  public SingleResult<HealthResp> handle(HttpServerRequest request) {
    HealthResp healthResp = new HealthResp();
    healthResp.setStatus("Up");
    healthResp.setTimestamp(Instant.now().toEpochMilli());

    return SingleResult.success(healthResp);
  }
}