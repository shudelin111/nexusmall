package com.nexusmall.logistics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轨迹状态枚举
 * <p>
 * 业界标准：
 * - 1=已揽件（快递员已取件）
 * - 2=运输中（包裹在转运中心之间运输）
 * - 3=派送中（快递员正在派送）
 * - 4=已签收（收件人已签收）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum TrackStatusEnum {

    /**
     * 已揽件
     */
    PICKED_UP(1, "已揽件"),

    /**
     * 运输中
     */
    IN_TRANSIT(2, "运输中"),

    /**
     * 派送中
     */
    DELIVERING(3, "派送中"),

    /**
     * 已签收
     */
    SIGNED(4, "已签收");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static TrackStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TrackStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
