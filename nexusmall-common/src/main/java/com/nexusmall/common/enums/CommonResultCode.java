package com.nexusmall.common.enums;

import lombok.Getter;

/**
 * 通用错误码枚举
 * <p>
 * 遵循业界最佳实践：
 * - httpStatus: HTTP 状态码，用于宏观错误分类
 * - errorCode: 业务错误码，格式为 XXXYYY（XXX 为模块号，YYY 为顺序号），全局唯一
 * - message: 用户友好的错误消息
 * </p>
 */
@Getter
public enum CommonResultCode implements ResultCode {
    
    // ===== 成功响应 (2xx) =====
    OK(200, "10000", "success"),
    
    // ===== 客户端错误 - 通用 (4xx) =====
    PARAM_INVALID(400, "20001", "参数错误"),
    INSUFFICIENT_STOCK(400, "20002", "库存不足"),
    USER_ALREADY_EXISTS(400, "20003", "用户名已存在"),
    
    UNAUTHORIZED(401, "20101", "未登录或登录已过期"),
    INVALID_CREDENTIALS(401, "20102", "用户名或密码错误"),
    
    FORBIDDEN(403, "20301", "无权限访问"),
    SENTINEL_AUTHORITY(403, "20302", "无权限访问该资源"),
    USER_DISABLED(403, "20303", "用户已被禁用"),
    
    NOT_FOUND(404, "20401", "资源不存在"),
    USER_NOT_FOUND(404, "20402", "用户不存在"),
    ROLE_NOT_FOUND(404, "20403", "角色不存在"),
    PERMISSION_NOT_FOUND(404, "20404", "权限不存在"),
    
    // ===== Sentinel 流控 (429/503) =====
    SENTINEL_FLOW(429, "21001", "访问过于频繁，请稍后再试"),
    SENTINEL_PARAM_FLOW(429, "21002", "访问过于频繁，请调整访问参数"),
    SYSTEM_BUSY(429, "21003", "系统繁忙，请稍后再试"),
    SENTINEL_DEGRADE(503, "21101", "服务暂时不可用，请稍后重试"),
    SENTINEL_SYSTEM(503, "21102", "系统繁忙，请稍后再试"),
    
    // ===== 服务端错误 - 通用 (500) =====
    SYSTEM_ERROR(500, "30001", "系统异常"),
    SENTINEL_UNKNOWN(500, "30002", "请求被拦截"),
    
    // ===== 服务端错误 - 分布式锁 (500) =====
    LOCK_FAILED(500, "30101", "获取分布式锁失败"),
    LOCK_INTERRUPTED(500, "30102", "获取分布式锁被中断"),
    
    // ===== 服务端错误 - 商品服务 (500) =====
    STOCK_OPERATION_FAILED(500, "30201", "库存操作失败"),
    
    // ===== 服务端错误 - 订单服务 (500) =====
    ORDER_CREATE_FAILED(500, "30301", "创建订单失败"),
    
    // ===== 服务端错误 - 第三方服务 (500) =====
    FILE_UPLOAD_FAILED(500, "30401", "文件上传失败"),
    OSS_CONFIG_ERROR(500, "30402", "OSS 配置错误"),
    SMS_SERVICE_ERROR(500, "30403", "短信服务异常"),
    JSON_SERIALIZE_FAILED(500, "30404", "JSON 序列化失败"),
    SMS_TEMPLATE_PARSE_FAILED(500, "30405", "短信模板解析失败"),
    
    // ===== 服务端错误 - 消息队列 (500) =====
    MQ_SEND_FAILED(500, "30501", "发送消息失败"),
    
    // ===== 服务端错误 - 认证服务 (500) =====
    USER_REGISTRATION_FAILED(500, "30601", "用户注册失败");

    /**
     * HTTP 状态码
     */
    private final int httpStatus;
    
    /**
     * 业务错误码（全局唯一）
     * 格式：XXXYYY
     * - XXX: 模块编号（1=成功，2=客户端错误，3=服务端错误）
     * - YYY: 顺序号
     */
    private final String errorCode;
    
    /**
     * 用户友好的错误消息
     */
    private final String message;

    CommonResultCode(int httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return httpStatus;
    }
    
    /**
     * 获取业务错误码
     * @return 业务错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
