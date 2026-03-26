package com.nexusmall.common.exception;

/**
 * 认证业务异常
 * <p>
 * 用于认证模块的业务异常，如用户不存在、密码错误等
 * </p>
 *
 * @author nexusmall
 */
public class AuthException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }

    public AuthException(Integer code, String message) {
        super(code, message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
