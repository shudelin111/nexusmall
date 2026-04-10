package com.nexusmall.common.vo;

import com.nexusmall.common.enums.ResultCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应结果封装（生产级标准版）
 * <p>
 * 遵循业界最佳实践：
 * - 禁止硬编码 code 和 message，必须通过 ResultCode 枚举构造
 * - 包含 timestamp 便于审计和问题排查
 * - 使用 @Getter 而非 @Data，避免暴露 setter
 * - traceId 由 Spring Cloud Sleuth 自动注入到 HTTP 响应头 X-B3-TraceId
 * </p>
 *
 * @param <T> 数据类型
 * @author nexusmall
 */
@Getter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private final boolean success;

    /**
     * 业务错误码（来自 ResultCode 枚举）
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    /**
     * 响应时间戳
     */
    private final LocalDateTime timestamp;

    /**
     * 私有构造函数，强制使用静态工厂方法
     */
    private Result(boolean success, String code, String message, T data, LocalDateTime timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(true, ResultCode.OK.getErrorCode(), ResultCode.OK.getMessage(), null, LocalDateTime.now());
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, ResultCode.OK.getErrorCode(), ResultCode.OK.getMessage(), data, LocalDateTime.now());
    }

    /**
     * 成功响应（自定义消息 + 数据）
     * <p>
     * 用于需要返回特定业务消息的场景，如"新增成功"、"删除成功"等
     * </p>
     *
     * @param message 自定义成功消息
     * @param data    响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ResultCode.OK.getErrorCode(), message, data, LocalDateTime.now());
    }

    /**
     * 失败响应（使用 ResultCode 枚举）
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return new Result<>(false, resultCode.getErrorCode(), resultCode.getMessage(), null, LocalDateTime.now());
    }

    /**
     * 失败响应（自定义错误码 + 消息）
     * <p>
     * 注意：此方法仅用于兼容历史代码，新代码应使用 ResultCode 枚举
     * </p>
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 失败响应结果
     * @deprecated 请使用 {@link #failure(ResultCode)} 替代
     */
    @Deprecated
    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null, LocalDateTime.now());
    }
}
