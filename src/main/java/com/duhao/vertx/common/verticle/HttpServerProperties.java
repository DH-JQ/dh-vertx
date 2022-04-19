package com.duhao.vertx.common.verticle;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Du Hao
 * @version 1.0
 * @since 2022/4/14
 */
@Data
@ConfigurationProperties(prefix = "vertx.server.http")
public class HttpServerProperties {
  public static final String DEFAULT_HANDLER_PATH = "/";

  private int port = 8080;

  private String handlerPath = DEFAULT_HANDLER_PATH;
}