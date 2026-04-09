package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;
import lombok.Getter;

/**
 * Sentinel 流控异常
 * <p>
 * 当触发 Sentinel 流控、熔断、降级规则时抛出此异常
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Getter
public class SentinelFlowException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    /**
     * 资源名称
     */
    private final String resourceName;

    public SentinelFlowException(ResultCode resultCode, String resourceName) {
        super(resultCode);
        this.resourceName = resourceName;
    }

    public SentinelFlowException(ResultCode resultCode, String resourceName, Throwable cause) {
        super(resultCode, cause);
        this.resourceName = resourceName;
    }
}
