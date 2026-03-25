package com.nexusmall.common.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 * 用于标记需要加分布式锁的方法
 * 
 * @author NexusMall
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的键名
     * 支持 SpEL 表达式，如 "#orderId"
     */
    String key() default "";

    /**
     * 等待时间（秒），默认 5 秒
     */
    long waitTime() default 5;

    /**
     * 锁持有时间（秒），默认 30 秒
     * -1 表示不自动释放
     */
    long leaseTime() default 30;
}
