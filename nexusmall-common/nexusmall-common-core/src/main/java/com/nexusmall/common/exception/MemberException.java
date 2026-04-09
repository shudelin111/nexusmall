package com.nexusmall.common.exception;

/**
 * 会员业务异常
 * <p>
 * 用于会员模块的业务异常，如会员不存在、会员已禁用等
 * </p>
 *
 * @author nexusmall
 */
public class MemberException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public MemberException(String message) {
        super(message);
    }

    public MemberException(Integer code, String message) {
        super(code, message);
    }

    public MemberException(String code, String message) {
        super(code, message);
    }

    public MemberException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public MemberException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
