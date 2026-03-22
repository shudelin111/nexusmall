package com.nexusmall.common.vo;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setSuccess(true);
        result.setCode(CommonResultCode.OK.getCode());
        result.setMessage(CommonResultCode.OK.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> failure(String code, String message) {
        Result<T> result = new Result<T>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> failure(ResultCode resultCode) {
        return failure(resultCode.getCode(), resultCode.getMessage());
    }
}
