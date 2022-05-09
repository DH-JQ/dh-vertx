package com.duhao.vertx.common.model.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * @author Hao Du
 * @version 1.0
 * @since 2022/4/24
 */
@Data
@Builder
public class ResponseEntity {
    private Integer statusCode;

    private Object body;
}
