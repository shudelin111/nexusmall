package com.nexusmall.common.aspect;

import com.nexusmall.common.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 AOP 切面
 * 
 * @author NexusMall
 */
@Aspect
@Component
@Order(1) // 确保切面优先级较高
public class DistributedLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知，处理带有@DistributedLock 注解的方法
     */
    @Around("@annotation(com.nexusmall.common.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解信息
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        
        // 解析锁的键名（支持 SpEL 表达式）
        String lockKey = parseLockKey(distributedLock.key(), joinPoint);
        
        // 获取锁
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = false;
        try {
            // 尝试获取锁
            isLocked = lock.tryLock(distributedLock.waitTime(), 
                                   distributedLock.leaseTime(), 
                                   TimeUnit.SECONDS);
            
            if (isLocked) {
                // 获取锁成功，执行业务逻辑
                return joinPoint.proceed();
            } else {
                // 获取锁失败
                throw new RuntimeException("获取分布式锁失败：" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            // 释放锁
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 解析锁的键名（支持 SpEL 表达式）
     */
    private String parseLockKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            // 如果没有指定 key，使用方法签名作为默认 key
            return joinPoint.getSignature().toShortString();
        }
        
        // 如果包含 # 符号，说明是 SpEL 表达式
        if (!keyExpression.contains("#")) {
            return keyExpression;
        }
        
        // 使用 SpEL 解析参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 添加方法参数到上下文
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        
        try {
            Expression expression = parser.parseExpression(keyExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : keyExpression;
        } catch (Exception e) {
            // 解析失败时返回原始表达式
            return keyExpression;
        }
    }
}
