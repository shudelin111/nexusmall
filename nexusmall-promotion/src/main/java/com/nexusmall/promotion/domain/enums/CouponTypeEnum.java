package com.nexusmall.promotion.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠类型枚举
 * <p>
 * 业界标准：使用枚举替代魔法数字
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum CouponTypeEnum {

    /**
     * 满减券
     */
    FULL_REDUCTION(1, "满减券"),

    /**
     * 折扣券
     */
    DISCOUNT(2, "折扣券"),

    /**
     * 立减券
     */
    INSTANT_REDUCTION(3, "立减券");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static CouponTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
