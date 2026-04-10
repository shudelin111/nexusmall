package com.nexusmall.common.enums;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.Getter;

/**
 * Sentinel 阻断异常类型枚举
 * <p>
 * 定义 Sentinel 各种异常类型与错误码、提示消息的映射关系
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Getter
public enum SentinelBlockType {

    /**
     * 流控异常
     */
    FLOW(FlowException.class, ResultCode.SENTINEL_FLOW, "流控"),

    /**
     * 熔断降级异常
     */
    DEGRADE(DegradeException.class, ResultCode.SENTINEL_DEGRADE, "熔断降级"),

    /**
     * 热点参数限流异常
     */
    PARAM_FLOW(ParamFlowException.class, ResultCode.SENTINEL_PARAM_FLOW, "热点参数限流"),

    /**
     * 系统保护异常
     */
    SYSTEM(SystemBlockException.class, ResultCode.SENTINEL_SYSTEM, "系统保护"),

    /**
     * 授权异常
     */
    AUTHORITY(AuthorityException.class, ResultCode.SENTINEL_AUTHORITY, "授权异常");

    /**
     * 异常类型
     */
    private final Class<? extends BlockException> exceptionClass;

    /**
     * 对应的错误码
     */
    private final ResultCode resultCode;

    /**
     * 异常类型名称（用于日志记录）
     */
    private final String name;

    SentinelBlockType(Class<? extends BlockException> exceptionClass, ResultCode resultCode, String name) {
        this.exceptionClass = exceptionClass;
        this.resultCode = resultCode;
        this.name = name;
    }

    /**
     * 根据异常实例获取阻断类型
     *
     * @param e BlockException
     * @return SentinelBlockType，未找到返回 null
     */
    public static SentinelBlockType fromException(BlockException e) {
        for (SentinelBlockType type : values()) {
            if (type.getExceptionClass().isInstance(e)) {
                return type;
            }
        }
        return null;
    }
}
