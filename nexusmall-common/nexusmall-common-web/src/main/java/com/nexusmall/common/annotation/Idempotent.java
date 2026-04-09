package com.nexusmall.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 * <p>
 * 生产级实践：防止接口重复提交，基于Redis实现分布式幂等性控制
 * </p>
 * 
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * @PostMapping("/orders")
 * @Idempotent(key = "#orderRequest.orderNo", expireTime = 5)
 * public Result<OrderVO> createOrder(@RequestBody OrderRequest orderRequest) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    
    /**
     * 幂等性key（支持SpEL表达式）
     * <p>
     * 示例：
     * - "#orderRequest.orderNo" 使用请求参数中的订单号
     * - "#userId + ':' + #productId" 组合多个参数
     * - "T(java.util.UUID).randomUUID().toString()" 生成随机UUID
     * </p>
     *
     * @return 幂等性key表达式
     */
    String key();
    
    /**
     * 过期时间（默认5秒）
     * <p>
     * 在此时间内相同key的请求会被拦截
     * </p>
     *
     * @return 过期时间
     */
    long expireTime() default 5;
    
    /**
     * 时间单位（默认秒）
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    
    /**
     * 错误提示信息
     *
     * @return 错误消息
     */
    String message() default "请勿重复提交";
}
