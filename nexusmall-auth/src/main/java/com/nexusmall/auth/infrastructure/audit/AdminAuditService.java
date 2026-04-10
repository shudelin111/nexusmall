package com.nexusmall.auth.infrastructure.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 管理操作审计日志服务
 * <p>
 * 生产级特性：
 * - 记录所有管理后台操作
 * - 结构化日志输出
 * - 支持异步发送到 Kafka（TODO）
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-08
 */
@Slf4j
@Component
public class AdminAuditService {

    /**
     * 记录管理操作审计日志
     *
     * @param auditLog 审计日志对象
     */
    public void logAudit(AdminAuditLog auditLog) {
        // 1. 生成操作 ID（如果未提供)
        if (auditLog.getOperationId() == null) {
            auditLog.setOperationId(UUID.randomUUID().toString());
        }

        // 2. 设置操作时间（如果未提供)
        if (auditLog.getOperateTime() == null) {
            auditLog.setOperateTime(LocalDateTime.now());
        }

        // 3. 结构化日志输出
        log.info("[审计日志-{}] type={}, operator={}, target={}, result={}, duration={}ms, ip={}",
                auditLog.getOperationId(),
                auditLog.getOperationType(),
                auditLog.getOperatorUsername(),
                buildTargetDescription(auditLog),
                auditLog.getResult(),
                auditLog.getDurationMs(),
                auditLog.getIpAddress());

        // 4. 如果有错误，记录详细信息
        if ("FAILED".equals(auditLog.getResult()) && auditLog.getErrorMessage() != null) {
            log.error("[审计日志-{}] 错误详情: {}", auditLog.getOperationId(), auditLog.getErrorMessage());
        }

        // TODO: 生产环境应异步发送到 Kafka 或写入数据库
        // auditLogProducer.send(auditLog);
    }

    /**
     * 构建目标对象描述
     */
    private String buildTargetDescription(AdminAuditLog auditLog) {
        return String.format("%s(id=%d)", 
                auditLog.getTargetType(), 
                auditLog.getTargetId());
    }

    /**
     * 快速创建成功的审计日志
     */
    public AdminAuditLog createSuccessLog(String operationType, 
                                          String operatorUsername,
                                          Long operatorUserId,
                                          Long targetId,
                                          String targetType,
                                          String description,
                                          String ipAddress,
                                          String userAgent,
                                          long startTime) {
        return AdminAuditLog.builder()
                .operationType(operationType)
                .operatorUsername(operatorUsername)
                .operatorUserId(operatorUserId)
                .targetId(targetId)
                .targetType(targetType)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result("SUCCESS")
                .operateTime(LocalDateTime.now())
                .durationMs(System.currentTimeMillis() - startTime)
                .build();
    }

    /**
     * 快速创建失败的审计日志
     */
    public AdminAuditLog createFailedLog(String operationType,
                                         String operatorUsername,
                                         Long operatorUserId,
                                         Long targetId,
                                         String targetType,
                                         String description,
                                         String errorMessage,
                                         String ipAddress,
                                         String userAgent,
                                         long startTime) {
        return AdminAuditLog.builder()
                .operationType(operationType)
                .operatorUsername(operatorUsername)
                .operatorUserId(operatorUserId)
                .targetId(targetId)
                .targetType(targetType)
                .description(description)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result("FAILED")
                .operateTime(LocalDateTime.now())
                .durationMs(System.currentTimeMillis() - startTime)
                .build();
    }
}
