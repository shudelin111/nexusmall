package com.nexusmall.common.aspect;

import com.nexusmall.common.annotation.Idempotent;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.NexusmallException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性AOP切面
 * <p>
 * 生产级实践：基于Redis实现分布式幂等性控制，防止重复提交
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
@Aspect
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    public IdempotentAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 解析SpEL表达式生成幂等性key
        String key = parseKey(joinPoint, idempotent.key());
        String redisKey = "idempotent:" + key;

        // 尝试设置Redis key（SETNX）
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", idempotent.expireTime(), idempotent.timeUnit());

        if (Boolean.FALSE.equals(success)) {
            // key已存在，说明是重复请求
            log.warn("【幂等性拦截】key: {}, 方法: {}", redisKey, joinPoint.getSignature());
            throw new NexusmallException(ResultCode.CONFLICT, idempotent.message());
        }

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } catch (Exception e) {
            // 业务异常时删除key，允许重试
            redisTemplate.delete(redisKey);
            log.error("【幂等性异常】key: {}, 错误: {}", redisKey, e.getMessage());
            throw e;
        }
    }

    /**
     * 解析SpEL表达式
     *
     * @param joinPoint 切点
     * @param keyExpression SpEL表达式
     * @return 解析后的key
     */
    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        
        // 获取参数名
        String[] paramNames = discoverer.getParameterNames(method);
        if (paramNames == null || paramNames.length == 0) {
            throw new IllegalArgumentException("无法获取方法参数名，请确保编译时保留调试信息");
        }

        // 构建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // 解析表达式
        Expression expression = parser.parseExpression(keyExpression);
        Object value = expression.getValue(context);
        
        if (value == null) {
            throw new IllegalArgumentException("幂等性key解析结果为null: " + keyExpression);
        }

        return value.toString();
    }
}
