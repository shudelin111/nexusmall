package com.nexusmall.common.aspect;

import com.nexusmall.common.annotation.Idempotent;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.NexusmallException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性 AOP 切面
 * <p>
 * 基于 Redis 分布式锁实现接口幂等性，防止重复提交
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
@Aspect
@Component
@ConditionalOnClass(RedissonClient.class)
public class IdempotentAspect {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知，处理带有@Idempotent 注解的方法
     */
    @Around("@annotation(com.nexusmall.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (redissonClient == null) {
            log.warn("【幂等性】RedissonClient 未配置，跳过幂等性检查");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解信息
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        
        // 生成幂等性键
        String idempotentKey = generateIdempotentKey(idempotent, joinPoint);
        
        // 获取分布式锁
        RLock lock = redissonClient.getLock("idempotent:" + idempotentKey);
        
        boolean isLocked = false;
        try {
            // 尝试获取锁（不等待，立即返回）
            isLocked = lock.tryLock(0, idempotent.expireTime(), TimeUnit.SECONDS);
            
            if (isLocked) {
                // 获取锁成功，执行业务逻辑
                log.debug("【幂等性】请求通过，key: {}", idempotentKey);
                return joinPoint.proceed();
            } else {
                // 获取锁失败，说明是重复请求
                log.warn("【幂等性】重复请求被拦截，key: {}", idempotentKey);
                throw new NexusmallException(
                    CommonResultCode.SYSTEM_BUSY.getErrorCode(),
                    idempotent.message()
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【幂等性】获取锁被中断，key: {}", idempotentKey, e);
            throw new NexusmallException(
                CommonResultCode.SYSTEM_ERROR.getErrorCode(),
                "系统异常，请稍后重试",
                e
            );
        } finally {
            // 释放锁
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 生成幂等性键
     */
    private String generateIdempotentKey(Idempotent idempotent, ProceedingJoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 添加前缀
        if (idempotent.prefix() != null && !idempotent.prefix().isEmpty()) {
            keyBuilder.append(idempotent.prefix()).append(":");
        } else {
            // 默认使用方法签名作为前缀
            keyBuilder.append(joinPoint.getSignature().toShortString()).append(":");
        }
        
        // 添加自定义键（支持 SpEL 表达式）
        if (idempotent.key() != null && !idempotent.key().isEmpty()) {
            String customKey = parseSpEL(idempotent.key(), joinPoint);
            keyBuilder.append(customKey);
        } else {
            // 如果没有指定 key，使用参数哈希值
            keyBuilder.append(java.util.Arrays.hashCode(joinPoint.getArgs()));
        }
        
        return keyBuilder.toString();
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseSpEL(String expression, ProceedingJoinPoint joinPoint) {
        if (!expression.contains("#")) {
            return expression;
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 添加方法参数到上下文
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        
        try {
            Expression expr = parser.parseExpression(expression);
            Object value = expr.getValue(context);
            return value != null ? value.toString() : expression;
        } catch (Exception e) {
            log.warn("【幂等性】SpEL 解析失败，使用原始表达式: {}", expression, e);
            return expression;
        }
    }
}
