package com.nexusmall.common.annotation;

import java.lang.annotation.*;

/**
 * 幂等性注解
 * <p>
 * 用于标记需要保证幂等性的接口，防止重复提交
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等性键的前缀
     * <p>
     * 默认为方法签名，可自定义
     * </p>
     */
    String prefix() default "";

    /**
     * 幂等性键的 SpEL 表达式
     * <p>
     * 例如：#userId、#orderNo
     * </p>
     */
    String key() default "";

    /**
     * 幂等性保持时间（秒）
     * <p>
     * 默认 60 秒，超过该时间后可以再次提交
     * </p>
     */
    long expireTime() default 60;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
