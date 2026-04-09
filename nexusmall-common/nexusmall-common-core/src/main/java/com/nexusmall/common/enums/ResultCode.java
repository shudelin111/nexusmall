package com.nexusmall.common.enums;

import lombok.Getter;

import java.util.Locale;

/**
 * 统一结果码枚举(生产级标准版)
 * <p>
 * 遵循业界最佳实践(Spring Framework HttpStatus + Alibaba规范):
 * - httpStatus: HTTP状态码,用于协议层错误分类
 * - errorCode: 业务错误码,格式为MMCCSS(6位数字),全局唯一
 *   - MM: 模块编号 (01-99)
 *     - 01: 通用模块
 *     - 02: 认证授权模块
 *     - 03: 会员模块
 *     - 04: 商品模块
 *     - 05: 订单模块
 *     - 06: 支付模块
 *     - 07: 购物车模块
 *     - 08: 库存模块
 *     - 09: 物流模块
 *     - 10: 营销模块
 *     - 11: 搜索模块
 *     - 12: 通知模块
 *     - 13: 第三方服务模块
 *     - 14: 网关模块
 *   - CC: 错误类型 (00-99)
 *     - 00: 参数校验错误
 *     - 01: 认证/授权错误
 *     - 02: 资源不存在
 *     - 03: 业务逻辑错误
 *     - 04: 系统异常
 *     - 05: 外部依赖错误
 *   - SS: 序号 (01-99)
 * - messageKey: 国际化消息键(用于多语言支持)
 * - message: 默认中文消息
 * </p>
 * <p>
 * <strong>设计原则:</strong>
 * - 删除ResultCode接口,只保留枚举(参考Spring HttpStatus)
 * - 删除所有模块专属枚举(OrderResultCode等),统一通过MM编码区分
 * - 所有异常构造函数只支持ResultCode枚举,禁止硬编码
 * </p>
 *
 * @author nexusmall
 */
@Getter
public enum ResultCode {
    
    // ===== 成功响应 (2xx) =====
    /**
     * 请求成功
     */
    OK(200, "010000", "success.ok", "操作成功"),
    
    // ===== 客户端错误 - 通用模块 (4xx) =====
    /**
     * 参数校验失败
     */
    PARAM_INVALID(400, "010001", "error.param.invalid", "参数错误"),
    
    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "010002", "error.method.not.allowed", "请求方法不支持"),
    
    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "010003", "error.request.timeout", "请求超时"),
    
    /**
     * 请求冲突（如重复提交）
     */
    CONFLICT(409, "010004", "error.conflict", "请求冲突"),
    
    // ===== 认证授权错误 (401/403) =====
    /**
     * 未登录或Token无效
     */
    UNAUTHORIZED(401, "020101", "error.auth.unauthorized", "未登录或登录已过期"),
    
    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS(401, "020102", "error.auth.invalid.credentials", "用户名或密码错误"),
    
    /**
     * Token无效
     */
    TOKEN_INVALID(401, "020103", "error.auth.token.invalid", "Token 无效"),
    
    /**
     * Token已过期
     */
    TOKEN_EXPIRED(401, "020104", "error.auth.token.expired", "Token 已过期"),
    
    /**
     * Token已被撤销
     */
    TOKEN_REVOKED(401, "020105", "error.auth.token.revoked", "Token 已被撤销"),
    
    /**
     * 刷新Token无效
     */
    REFRESH_TOKEN_INVALID(401, "020106", "error.auth.refresh.token.invalid", "刷新Token无效"),
    
    /**
     * 无权限访问
     */
    FORBIDDEN(403, "020107", "error.auth.forbidden", "无权限访问"),
    
    /**
     * Sentinel权限拦截
     */
    SENTINEL_AUTHORITY(403, "020108", "error.sentinel.authority", "无权限访问该资源"),
    
    /**
     * 用户已被禁用
     */
    USER_DISABLED(403, "020109", "error.auth.user.disabled", "用户已被禁用"),
    
    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(400, "020301", "error.auth.user.already.exists", "用户已存在"),
    
    /**
     * 用户注册失败
     */
    USER_REGISTRATION_FAILED(500, "020401", "error.auth.user.registration.failed", "用户注册失败"),
    
    // ===== 资源不存在 (404) =====
    /**
     * 资源不存在（通用）
     */
    NOT_FOUND(404, "010201", "error.resource.not.found", "资源不存在"),
    
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(404, "030201", "error.user.not.found", "用户不存在"),
    
    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND(404, "030202", "error.role.not.found", "角色不存在"),
    
    /**
     * 权限不存在
     */
    PERMISSION_NOT_FOUND(404, "030203", "error.permission.not.found", "权限不存在"),
    
    // ===== 限流降级 (429/503) =====
    /**
     * 访问频率过高（流控）
     */
    RATE_LIMITED(429, "010501", "error.rate.limited", "访问过于频繁，请稍后再试"),
    
    /**
     * Sentinel流控拦截
     */
    SENTINEL_FLOW(429, "010502", "error.sentinel.flow", "访问过于频繁，请稍后再试"),
    
    /**
     * Sentinel参数流控
     */
    SENTINEL_PARAM_FLOW(429, "010503", "error.sentinel.param.flow", "访问过于频繁，请调整访问参数"),
    
    /**
     * 系统繁忙（Sentinel系统保护）
     */
    SYSTEM_BUSY(429, "010504", "error.system.busy", "系统繁忙，请稍后再试"),
    
    /**
     * 服务熔断中
     */
    CIRCUIT_BREAKER_OPEN(503, "010505", "error.circuit.breaker.open", "服务暂时不可用，请稍后重试"),
    
    /**
     * Sentinel降级
     */
    SENTINEL_DEGRADE(503, "010506", "error.sentinel.degrade", "服务暂时不可用，请稍后重试"),
    
    /**
     * Sentinel系统保护
     */
    SENTINEL_SYSTEM(503, "010507", "error.sentinel.system", "系统繁忙，请稍后再试"),
    
    // ===== 服务端错误 - 通用 (500) =====
    /**
     * 系统异常（通用）
     */
    SYSTEM_ERROR(500, "010401", "error.system.error", "系统异常"),
    
    /**
     * Sentinel未知拦截
     */
    SENTINEL_UNKNOWN(500, "010402", "error.sentinel.unknown", "请求被拦截"),
    
    // ===== 分布式锁错误 (500) =====
    /**
     * 获取分布式锁失败
     */
    LOCK_FAILED(500, "010403", "error.lock.failed", "获取分布式锁失败"),
    
    /**
     * 获取分布式锁被中断
     */
    LOCK_INTERRUPTED(500, "010404", "error.lock.interrupted", "获取分布式锁被中断"),
    
    /**
     * 获取锁超时
     */
    LOCK_ACQUIRE_TIMEOUT(500, "010405", "error.lock.timeout", "获取锁超时"),
    
    /**
     * 锁冲突
     */
    LOCK_CONFLICT(500, "010406", "error.lock.conflict", "锁冲突"),
    
    /**
     * JSON序列化失败
     */
    JSON_SERIALIZE_FAILED(500, "010407", "error.json.serialize.failed", "JSON 序列化失败"),
    
    // ===== 商品服务错误 =====
    /**
     * 库存不足
     */
    INSUFFICIENT_STOCK(400, "040301", "error.stock.insufficient", "库存不足"),
    
    /**
     * 库存操作失败
     */
    STOCK_OPERATION_FAILED(500, "040401", "error.stock.operation.failed", "库存操作失败"),
    
    // ===== 订单服务错误 =====
    /**
     * 创建订单失败
     */
    ORDER_CREATE_FAILED(500, "050401", "error.order.create.failed", "创建订单失败"),
    
    // ===== 支付服务错误 =====
    /**
     * 支付失败
     */
    PAYMENT_FAILED(500, "060401", "error.payment.failed", "支付失败"),
    
    /**
     * 支付超时
     */
    PAYMENT_TIMEOUT(500, "060402", "error.payment.timeout", "支付超时"),
    
    /**
     * 支付渠道不可用
     */
    PAYMENT_CHANNEL_UNAVAILABLE(500, "060403", "error.payment.channel.unavailable", "支付渠道暂时不可用"),
    
    /**
     * 退款失败
     */
    REFUND_FAILED(500, "060404", "error.refund.failed", "退款失败"),
    
    /**
     * 支付单不存在
     */
    PAYMENT_NOT_FOUND(404, "060201", "error.payment.not.found", "支付单不存在"),
    
    /**
     * 退款单不存在
     */
    REFUND_NOT_FOUND(404, "060202", "error.refund.not.found", "退款单不存在"),
    
    /**
     * 支付金额无效
     */
    INVALID_PAYMENT_AMOUNT(400, "060301", "error.payment.amount.invalid", "支付金额无效"),
    
    // ===== 购物车服务错误 =====
    /**
     * 购物车已满
     */
    CART_FULL(400, "070301", "error.cart.full", "购物车已满，最多可添加100种商品"),
    
    /**
     * 购物车项不存在
     */
    CART_ITEM_NOT_FOUND(404, "070201", "error.cart.item.not.found", "购物车商品不存在"),
    
    /**
     * 购物车数量无效
     */
    CART_QUANTITY_INVALID(400, "070001", "error.cart.quantity.invalid", "数量必须大于0"),
    
    /**
     * 商品不在购物车中
     */
    CART_ITEM_NOT_IN_CART(400, "070302", "error.cart.item.not.in.cart", "商品不在购物车中"),
    
    // ===== 库存服务错误 =====
    /**
     * 库存不足（通用）
     */
    STOCK_INSUFFICIENT(400, "080301", "error.stock.insufficient", "库存不足"),
    
    /**
     * 库存扣减失败
     */
    STOCK_DEDUCT_FAILED(500, "080401", "error.stock.deduct.failed", "库存扣减失败"),
    
    /**
     * 库存回滚失败
     */
    STOCK_ROLLBACK_FAILED(500, "080402", "error.stock.rollback.failed", "库存回滚失败"),
    
    /**
     * 库存锁定失败
     */
    STOCK_LOCK_FAILED(500, "080403", "error.stock.lock.failed", "库存锁定失败"),
    
    // ===== 物流服务错误 =====
    /**
     * 物流信息不存在
     */
    LOGISTICS_NOT_FOUND(404, "090201", "error.logistics.not.found", "物流信息不存在"),
    
    /**
     * 物流跟踪失败
     */
    LOGISTICS_TRACKING_FAILED(500, "090401", "error.logistics.tracking.failed", "物流跟踪失败"),
    
    // ===== 营销服务错误 =====
    /**
     * 优惠券不存在
     */
    COUPON_NOT_FOUND(404, "100201", "error.coupon.not.found", "优惠券不存在"),
    
    /**
     * 优惠券已过期
     */
    COUPON_EXPIRED(400, "100301", "error.coupon.expired", "优惠券已过期"),
    
    /**
     * 优惠券已使用
     */
    COUPON_USED(400, "100302", "error.coupon.used", "优惠券已使用"),
    
    /**
     * 优惠券不满足使用条件
     */
    COUPON_CONDITION_NOT_MET(400, "100303", "error.coupon.condition.not.met", "不满足优惠券使用条件"),
    
    /**
     * 促销活动不存在
     */
    PROMOTION_NOT_FOUND(404, "100202", "error.promotion.not.found", "促销活动不存在"),
    
    /**
     * 促销活动已结束
     */
    PROMOTION_ENDED(400, "100304", "error.promotion.ended", "促销活动已结束"),
    
    // ===== 搜索服务错误 =====
    /**
     * 搜索索引失败
     */
    SEARCH_INDEX_FAILED(500, "110401", "error.search.index.failed", "搜索索引失败"),
    
    /**
     * 搜索查询失败
     */
    SEARCH_QUERY_FAILED(500, "110402", "error.search.query.failed", "搜索查询失败"),
    
    // ===== 通知服务错误 =====
    /**
     * 短信发送失败
     */
    SMS_SEND_FAILED(500, "120401", "error.sms.send.failed", "短信发送失败"),
    
    /**
     * 邮件发送失败
     */
    EMAIL_SEND_FAILED(500, "120402", "error.email.send.failed", "邮件发送失败"),
    
    /**
     * 推送通知失败
     */
    PUSH_NOTIFICATION_FAILED(500, "120403", "error.push.notification.failed", "推送通知失败"),
    
    // ===== 第三方服务错误 =====
    /**
     * OSS配置错误
     */
    OSS_CONFIG_ERROR(500, "130400", "error.oss.config.error", "OSS配置错误"),
        
    /**
     * OSS上传失败
     */
    OSS_UPLOAD_FAILED(500, "130401", "error.oss.upload.failed", "文件上传失败"),
        
    /**
     * OSS删除失败
     */
    OSS_DELETE_FAILED(500, "130402", "error.oss.delete.failed", "文件删除失败"),
        
    /**
     * 文件上传失败（通用）
     */
    FILE_UPLOAD_FAILED(500, "130403", "error.file.upload.failed", "文件上传失败"),
        
    /**
     * 短信服务错误
     */
    SMS_SERVICE_ERROR(500, "130404", "error.sms.service.error", "短信服务不可用"),
        
    /**
     * 短信模板解析失败
     */
    SMS_TEMPLATE_PARSE_FAILED(500, "130405", "error.sms.template.parse.failed", "短信模板解析失败"),
        
    /**
     * 第三方API调用失败
     */
    THIRD_PARTY_API_FAILED(500, "130406", "error.third.party.api.failed", "第三方服务调用失败"),
    
    // ===== 网关服务错误 =====
    /**
     * 路由不存在
     */
    ROUTE_NOT_FOUND(404, "140201", "error.route.not.found", "路由不存在"),
    
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "140501", "error.service.unavailable", "服务暂时不可用");

    /**
     * HTTP 状态码（用于协议层）
     */
    private final int httpStatus;
    
    /**
     * 业务错误码（全局唯一，6位数字）
     * 格式：MMCCSS
     * - MM: 模块编号 (01-99)
     * - CC: 错误类型 (00-99)
     * - SS: 序号 (01-99)
     */
    private final String errorCode;
    
    /**
     * 国际化消息键（用于多语言支持）
     */
    private final String messageKey;
    
    /**
     * 默认中文消息（兜底显示）
     */
    private final String message;

    ResultCode(int httpStatus, String errorCode, String messageKey, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.message = message;
    }

    /**
     * 获取消息（当前仅支持中文）
     * <p>
     * 注意：当前版本暂不支持国际化，直接返回默认中文消息。
     * 保留 locale 参数是为了未来扩展性，如需支持多语言，可集成 Spring MessageSource。
     * </p>
     *
     * @param locale 语言环境（当前未使用，保留用于未来扩展）
     * @return 默认中文消息
     */
    public String getMessage(Locale locale) {
        // 当前版本仅支持中文
        return message;
    }
}
