package com.nexusmall.search.application.dto;

import java.time.LocalDateTime;

/**
 * 索引操作审计日志 DTO
 * <p>
 * 生产级特性：
 * - 完整记录操作上下文
 * - 支持操作追溯
 * - 便于问题排查
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-08
 */
public class IndexOperationAuditDTO {

    /**
     * 操作 ID（唯一标识）
     */
    private String operationId;

    /**
     * 操作类型（BUILD/REMOVE/BATCH_REMOVE/REBUILD/CLEAR_ALL）
     */
    private String operationType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作原因
     */
    private String reason;

    /**
     * 操作对象（商品ID列表或描述）
     */
    private String targetDescription;

    /**
     * 操作结果（SUCCESS/FAILED/PARTIAL_SUCCESS）
     */
    private String result;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
