package com.nexusmall.common.enums;

public enum CommonResultCode implements ResultCode {

    OK("OK", "success"),
    PARAM_INVALID("PARAM_INVALID", "参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未登录或登录已过期"),
    FORBIDDEN("FORBIDDEN", "无权限访问"),
    NOT_FOUND("NOT_FOUND", "资源不存在"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常");

    private final String code;
    private final String message;

    CommonResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
