package com.nexusmall.common.enums;

public enum CommonResultCode implements ResultCode {

    OK(200, "success"),
    PARAM_INVALID(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_BUSY(429, "系统繁忙，请稍后再试"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    CommonResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
