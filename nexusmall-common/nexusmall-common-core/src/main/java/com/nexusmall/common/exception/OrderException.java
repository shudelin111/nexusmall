package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 订单业务异常
 * <p>
 * 用于订单模块的业务异常，如订单不存在、库存不足等
 * </p>
 * <p>
 * <strong>生产级使用规范：</strong>
 * <ul>
 *   <li>优先使用 {@link #OrderException(ResultCode)} - 标准用法</li>
 *   <li>仅在需要补充具体上下文时使用 {@link #OrderException(ResultCode, String)}</li>
 *   <li>需要保留原始异常链时使用 {@link #OrderException(ResultCode, String, Throwable)}</li>
 * </ul>
 * </p>
 *
 * @author nexusmall
 */
public class OrderException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public OrderException(ResultCode resultCode) {
        super(resultCode);
    }

    public OrderException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    /**
     * 使用 ResultCode 和自定义消息构造异常
     * <p>
     * <strong>使用场景：</strong>需要在标准错误消息基础上补充具体上下文信息
     * </p>
     * <p>
     * <strong>示例：</strong>
     * <pre>{@code
     * // ✅ 正确：补充具体上下文
     * throw new OrderException(ResultCode.SYSTEM_ERROR, 
     *     "MQ发送失败: topic=" + topic + ", tag=" + tag);
     * 
     * // ❌ 错误：完全忽略 ResultCode 的语义
     * throw new OrderException(ResultCode.SYSTEM_ERROR, "随便写的错误消息");
     * }</pre>
     * </p>
     *
     * @param resultCode 结果码枚举（错误码来源）
     * @param message    自定义错误消息（补充上下文，非替代 ResultCode 消息）
     */
    public OrderException(ResultCode resultCode, String message) {
        super(resultCode, message);
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
    public OrderException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }
}
