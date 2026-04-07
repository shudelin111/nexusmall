package com.nexusmall.promotion.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券使用状态枚举
 * <p>
 * 业界标准：清晰的状态流转，支持完整的生命周期管理
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum CouponUseStatusEnum {

    /**
     * 未使用
     */
    UNUSED(0, "未使用"),

    /**
     * 已使用
     */
    USED(1, "已使用"),

    /**
     * 已过期
     */
    EXPIRED(2, "已过期"),

    /**
     * 已锁定（下单未支付）
     */
    LOCKED(3, "已锁定");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static CouponUseStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponUseStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
