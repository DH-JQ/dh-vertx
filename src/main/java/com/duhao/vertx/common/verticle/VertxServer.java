package com.duhao.vertx.common.verticle;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.vertx.core.Vertx;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/7
 */
public class VertxServer {

  @Resource
  private Vertx vertx;

  @Resource
  private HttpVerticle httpVerticle;

  @PostConstruct
  public void start() {
    vertx.deployVerticle(httpVerticle);
  }
}