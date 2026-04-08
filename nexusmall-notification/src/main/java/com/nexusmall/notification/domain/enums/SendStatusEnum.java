package com.nexusmall.notification.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送状态枚举
 * <p>
 * 业界标准：
 * - 统一状态管理
 * - 支持异步发送场景
 * - 便于监控和重试
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum SendStatusEnum {

    /** 待发送 */
    PENDING(0, "待发送"),

    /** 发送成功 */
    SUCCESS(1, "发送成功"),

    /** 发送失败 */
    FAILED(2, "发送失败");

    /**
     * 状态代码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 状态代码
     * @return 枚举值
     */
    public static SendStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SendStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
