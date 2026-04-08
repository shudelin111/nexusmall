package com.nexusmall.auth.infrastructure.audit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理操作审计日志
 * <p>
 * 生产级特性：
 * - 记录所有管理后台操作
 * - 支持操作追溯和合规审计
 * - TODO: 持久化到数据库或发送到 Kafka
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-08
 */
@Data
@Builder
public class AdminAuditLog {

    /**
     * 操作 ID（UUID）
     */
    private String operationId;

    /**
     * 操作类型
     */
    private String operationType; // REGISTER_USER, UPDATE_USER, DELETE_USER, ASSIGN_ROLE, ASSIGN_PERMISSION

    /**
     * 操作人用户名
     */
    private String operatorUsername;

    /**
     * 操作人用户 ID
     */
    private Long operatorUserId;

    /**
     * 目标对象 ID
     */
    private Long targetId;

    /**
     * 目标对象类型
     */
    private String targetType; // USER, ROLE, PERMISSION

    /**
     * 操作描述
     */
    private String description;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 操作结果
     */
    private String result; // SUCCESS, FAILED

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;
}
