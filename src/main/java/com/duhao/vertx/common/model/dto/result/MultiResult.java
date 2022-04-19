package com.duhao.vertx.common.model.dto.result;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hao Du
 * @version 1.0
 * @date 2021/6/3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiResult<T extends Serializable> extends Result<T> {
    private static final long serialVersionUID = -3695351155879002469L;

    private Collection<T> items;

    protected MultiResult() {
        super();
    }

    protected MultiResult(Integer errCode, String errMessage, String userTip, boolean success, Collection<T> items) {
        super(errCode, errMessage, userTip, success);
        this.items = items;
    }

    public Collection<T> getItems() {
        return items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    public static <E extends Serializable> MultiResult<E> success(Collection<E> data) {
        return new MultiResult<>(null, null, null, true, data);
    }

    public static MultiResult<String> fail(Integer errCode, String errMessage) {
        return new MultiResult<>(errCode, errMessage, null, false, null);
    }

    public static MultiResult<String> fail(Integer errCode, String errMessage, String userTip) {
        return new MultiResult<>(errCode, errMessage, userTip, false, null);
    }
}