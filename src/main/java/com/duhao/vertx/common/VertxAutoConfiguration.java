package com.duhao.vertx.common;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/14
 */

import com.duhao.vertx.common.verticle.DispatcherHandler;
import com.duhao.vertx.common.verticle.HttpServerProperties;
import com.duhao.vertx.common.verticle.HttpVerticle;
import com.duhao.vertx.common.verticle.VertxServer;
import io.vertx.core.Vertx;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpServerProperties.class)
@ConditionalOnProperty(prefix = "vertx.server.http", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "com.duhao.vertx.common")
public class VertxAutoConfiguration {

  @Bean
  public Vertx vertx() {
    return Vertx.vertx();
  }

  @Bean
  public HttpVerticle httpVerticle() {
    return new HttpVerticle();
  }

  @Bean
  public DispatcherHandler dispatcherHandler() {
    return new DispatcherHandler();
  }

  @Bean
  public VertxServer vertxServer() {
    return new VertxServer();
  }


}