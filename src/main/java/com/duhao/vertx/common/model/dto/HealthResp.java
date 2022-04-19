package com.duhao.vertx.common.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Hao Du
 * @version 1.0
 * @since 2022/4/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResp implements Serializable {

    private String status;

    private Long timestamp;
}
