package com.duhao.vertx.common.model.dto.result;

import com.duhao.vertx.common.model.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * @author Hao Du
 * @version 1.0
 * @since 2021/9/24
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements BaseDTO {
    private static final long serialVersionUID = -2763435430428006217L;

    private Boolean success;

    private String traceId;

    private Integer code;

    private String message;

    private String userTip;

    protected Result() {
    }

    protected Result(boolean success) {
        this(null, null, null, success);
    }

    protected Result(Integer code, String message, String userTip, boolean success) {
        this.code = code;
        this.message = message;
        this.userTip = userTip;
        this.success = success;
    }

    public static Result<String> success() {
        return new Result<>(true);
    }

    public static Result<String> fail(Integer errCode, String errMessage, String userTip) {
        return new Result<>(errCode, errMessage, userTip, false);
    }

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserTip() {
        return userTip;
    }

    public void setUserTip(String userTip) {
        this.userTip = userTip;
    }
}