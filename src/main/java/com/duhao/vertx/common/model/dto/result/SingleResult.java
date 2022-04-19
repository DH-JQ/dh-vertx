package com.duhao.vertx.common.model.dto.result;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hao Du
 * @version 1.0
 * @date 2021/6/3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleResult<T extends Serializable> extends Result<T> {
    private static final long serialVersionUID = -3695351155879002469L;

    private  T data;

    protected SingleResult() {
        super();
    }

    protected SingleResult(Integer errCode, String errMessage, String userTip, boolean success, T data) {
        super(errCode, errMessage, userTip, success);
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <E extends Serializable> SingleResult<E> success(E data) {
        return new SingleResult<>(null, null, null, true, data);
    }

    public static SingleResult<String> fail(Integer errCode, String errMessage) {
        return new SingleResult<>(errCode, errMessage, null, false, null);
    }

    public static SingleResult<String> fail(Integer errCode, String errMessage, String userTip) {
        return new SingleResult<>(errCode, errMessage, userTip, false, null);
    }

    public static <E extends Serializable> SingleResult<E> fail(Integer errCode, String errMessage, String userTip, E data) {
        return new SingleResult<>(errCode, errMessage, userTip, false, data);
    }
}