package com.nexusmall.logistics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物流状态枚举
 * <p>
 * 业界标准：
 * - 0=待发货（订单已创建，等待仓库拣货）
 * - 1=已发货（包裹已交给快递公司）
 * - 2=运输中（包裹在运输途中）
 * - 3=已签收（客户已签收）
 * - 4=异常（物流异常，需人工介入）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum LogisticsStatusEnum {

    /**
     * 待发货
     */
    WAITING_SHIP(0, "待发货"),

    /**
     * 已发货
     */
    SHIPPED(1, "已发货"),

    /**
     * 运输中
     */
    IN_TRANSIT(2, "运输中"),

    /**
     * 已签收
     */
    SIGNED(3, "已签收"),

    /**
     * 异常
     */
    EXCEPTION(4, "异常");

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
    public static LogisticsStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LogisticsStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
