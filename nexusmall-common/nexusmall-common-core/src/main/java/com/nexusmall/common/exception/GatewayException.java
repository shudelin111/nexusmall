package com.nexusmall.common.exception;

/**
 * 网关业务异常
 * <p>
 * 用于网关模块的业务异常，如 Token 验证失败、路由错误等
 * </p>
 *
 * @author nexusmall
 */
public class GatewayException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(Integer code, String message) {
        super(code, message);
    }

    public GatewayException(String code, String message) {
        super(code, message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public GatewayException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public GatewayException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
