package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 认证业务异常
 * <p>
 * 用于认证模块的业务异常，如用户不存在、密码错误等
 * </p>
 * <p>
 * <strong>生产级使用规范：</strong>
 * <ul>
 *   <li>优先使用 {@link #AuthException(ResultCode)} - 标准用法</li>
 *   <li>仅在需要补充具体上下文时使用 {@link #AuthException(ResultCode, String)}</li>
 *   <li>需要保留原始异常链时使用 {@link #AuthException(ResultCode, String, Throwable)}</li>
 * </ul>
 * </p>
 *
 * @author nexusmall
 */
public class AuthException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }

    /**
     * 使用 ResultCode 和自定义消息构造异常
     * <p>
     * <strong>使用场景：</strong>需要在标准错误消息基础上补充具体上下文信息
     * </p>
     *
     * @param resultCode 结果码枚举（错误码来源）
     * @param message    自定义错误消息（补充上下文，非替代 ResultCode 消息）
     */
    public AuthException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public AuthException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    /**
     * 使用 ResultCode、自定义消息和异常原因构造异常
     * <p>
     * <strong>使用场景：</strong>需要保留原始异常链以便排查根因
     * </p>
     *
     * @param resultCode 结果码枚举（错误码来源）
     * @param message    自定义错误消息（补充上下文）
     * @param cause      原始异常（用于异常链追踪）
     */
    public AuthException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }
}
