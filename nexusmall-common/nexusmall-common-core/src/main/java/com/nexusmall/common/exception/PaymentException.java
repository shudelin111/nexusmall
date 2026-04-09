package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 支付业务异常
 * <p>
 * 用于支付模块的业务异常，如支付单不存在、支付失败等
 * </p>
 * <p>
 * <strong>生产级使用规范：</strong>
 * <ul>
 *   <li>优先使用 {@link #PaymentException(ResultCode)} - 标准用法</li>
 *   <li>仅在需要补充具体上下文时使用 {@link #PaymentException(ResultCode, String)}</li>
 *   <li>需要保留原始异常链时使用 {@link #PaymentException(ResultCode, String, Throwable)}</li>
 * </ul>
 * </p>
 *
 * @author nexusmall
 */
public class PaymentException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public PaymentException(ResultCode resultCode) {
        super(resultCode);
    }

    public PaymentException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    /**
     * 构造支付异常，支持补充具体上下文信息
     * <p>
     * <strong>使用场景：</strong>当ResultCode中的默认消息不足以描述具体问题时使用
     * </p>
     * <p>
     * <strong>示例：</strong>
     * <pre>{@code
     * // ✅ 正确：补充具体的支付单号
     * throw new PaymentException(ResultCode.PAYMENT_NOT_FOUND, "支付单PAY123456不存在");
     * 
     * // ❌ 错误：不要重复ResultCode中已有的信息
     * throw new PaymentException(ResultCode.INVALID_PAYMENT_AMOUNT, "支付金额无效");
     * }</pre>
     * </p>
     *
     * @param resultCode 结果码枚举
     * @param message 补充的上下文信息
     */
    public PaymentException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    /**
     * 构造支付异常，支持补充上下文并保留原始异常链
     * <p>
     * <strong>使用场景：</strong>需要包装底层异常（如网络异常、数据库异常）时使用
     * </p>
     *
     * @param resultCode 结果码枚举
     * @param message 补充的上下文信息
     * @param cause 原始异常
     */
    public PaymentException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }
}
