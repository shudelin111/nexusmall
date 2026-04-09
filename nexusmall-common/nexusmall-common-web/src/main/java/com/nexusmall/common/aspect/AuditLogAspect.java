package com.nexusmall.common.aspect;

import com.nexusmall.common.annotation.AuditLog;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 审计日志AOP切面
 * <p>
 * 生产级实践：记录关键操作的审计日志，包括操作人、IP、耗时等
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
@Aspect
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String ip = request != null ? getClientIp(request) : "unknown";
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = joinPoint.getSignature().toShortString();
        
        // 执行目标方法
        try {
            Object result = joinPoint.proceed();
            
            long costTime = System.currentTimeMillis() - startTime;
            
            // 判断业务结果是否成功
            boolean isSuccess = true;
            String errorMsg = null;
            
            if (result instanceof Result) {
                Result<?> resultVO = (Result<?>) result;
                isSuccess = resultVO.isSuccess();
                if (!isSuccess) {
                    errorMsg = resultVO.getMessage();
                }
            }
            
            // 记录审计日志
            if (isSuccess) {
                log.info("【审计日志】模块:{}, 操作:{}, URI:{}, 方法:{}, IP:{}, 耗时:{}ms, 结果:成功",
                        auditLog.module(),
                        auditLog.operation(),
                        uri,
                        method,
                        ip,
                        costTime);
            } else {
                log.warn("【审计日志】模块:{}, 操作:{}, URI:{}, 方法:{}, IP:{}, 耗时:{}ms, 结果:失败, 错误:{}",
                        auditLog.module(),
                        auditLog.operation(),
                        uri,
                        method,
                        ip,
                        costTime,
                        errorMsg);
            }
            
            // 如果需要记录参数
            if (auditLog.logParams()) {
                log.debug("【审计日志】请求参数: {}", joinPoint.getArgs());
            }
            
            // 如果需要记录结果
            if (auditLog.logResult()) {
                log.debug("【审计日志】响应结果: {}", result);
            }
            
            return result;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            
            // 记录异常日志
            log.error("【审计日志】模块:{}, 操作:{}, URI:{}, 方法:{}, IP:{}, 耗时:{}ms, 结果:异常, 错误:{}",
                    auditLog.module(),
                    auditLog.operation(),
                    uri,
                    method,
                    ip,
                    costTime,
                    e.getMessage());
            
            throw e;
        }
    }

    /**
     * 获取客户端真实IP
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
