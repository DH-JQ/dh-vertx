package com.duhao.vertx.common.model.dto.req;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Hao Du
 * @version 1.0
 * @date 2021/6/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery<C extends Serializable> implements Query {
    private static final long serialVersionUID = 977420243447692146L;

    private Integer current;

    private Integer size;

    private Integer limit;

    private Long offset;

    private String sort;

    private Boolean desc;

    private C condition;
}