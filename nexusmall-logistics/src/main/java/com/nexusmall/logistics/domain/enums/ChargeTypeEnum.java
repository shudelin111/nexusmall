package com.nexusmall.logistics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计费方式枚举
 * <p>
 * 业界标准：
 * - 1=按重量（最常见，适合大多数商品）
 * - 2=按体积（适合轻泡货物）
 * - 3=按件数（适合标准化商品）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum ChargeTypeEnum {

    /**
     * 按重量
     */
    BY_WEIGHT(1, "按重量"),

    /**
     * 按体积
     */
    BY_VOLUME(2, "按体积"),

    /**
     * 按件数
     */
    BY_PIECE(3, "按件数");

    /**
     * 类型码
     */
    private final Integer code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 枚举值
     */
    public static ChargeTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChargeTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
