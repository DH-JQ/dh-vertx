package com.duhao.vertx.common.model.po;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Hao Du
 * @version 1.0
 * @date 2021/6/3
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class JpaDO implements BaseDO {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Date createTime;

    private Date updatedTime;

    private Long creator;

    private Long updater;

}