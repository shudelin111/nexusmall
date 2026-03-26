package com.nexusmall.common.exception;

/**
 * Sentinel 流控异常
 * <p>
 * 当触发 Sentinel 流控、熔断、降级规则时抛出此异常
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
public class SentinelFlowException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    /**
     * 资源名称
     */
    private String resourceName;

    public SentinelFlowException(String message) {
        super(message);
    }

    public SentinelFlowException(String resourceName, String message) {
        super(message);
        this.resourceName = resourceName;
    }

    public SentinelFlowException(String resourceName, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
