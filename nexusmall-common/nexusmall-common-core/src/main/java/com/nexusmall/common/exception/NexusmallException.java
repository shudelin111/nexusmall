package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;
import lombok.Getter;

/**
 * Nexusmall 业务异常基类
 * <p>
 * 所有业务相关的异常都应该继承此类，便于统一处理和识别
 * </p>
 * <p>
 * <strong>生产级标准：</strong>
 * <ul>
 *   <li>只支持 ResultCode 枚举构造，禁止硬编码 code 和 message</li>
 *   <li>参考 Spring Framework 和阿里巴巴规范</li>
 *   <li>自定义消息仅用于补充上下文，不能替代 ResultCode 的语义</li>
 * </ul>
 * </p>
 *
 * @author nexusmall
 */
@Getter
public class NexusmallException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 结果码枚举（唯一数据源）
     */
    private final ResultCode resultCode;

    /**
     * 使用 ResultCode 构造异常
     *
     * @param resultCode 结果码枚举
     */
    public NexusmallException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 使用 ResultCode 构造异常（带异常原因）
     *
     * @param resultCode 结果码枚举
     * @param cause 异常原因
     */
    public NexusmallException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
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
     * throw new NexusmallException(ResultCode.SYSTEM_ERROR, 
     *     "MQ发送失败: topic=" + topic);
     * 
     * // ❌ 错误：完全忽略 ResultCode 的语义
     * throw new NexusmallException(ResultCode.SYSTEM_ERROR, "随便写的错误消息");
     * }</pre>
     * </p>
     *
     * @param resultCode 结果码枚举（错误码来源）
     * @param message    自定义错误消息（补充上下文，非替代 ResultCode 消息）
     */
    public NexusmallException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
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
    public NexusmallException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

    /**
     * 获取业务错误码（便捷方法）
     *
     * @return 错误码字符串
     */
    public String getCode() {
        return resultCode.getErrorCode();
    }
}
