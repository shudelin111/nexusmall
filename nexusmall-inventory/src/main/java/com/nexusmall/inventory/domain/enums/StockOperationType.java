package com.nexusmall.inventory.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存操作类型枚举
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum StockOperationType {

    /**
     * 采购入库
     */
    PURCHASE_IN("PURCHASE_IN", "采购入库"),

    /**
     * 订单扣减（锁定库存）
     */
    ORDER_DEDUCT("ORDER_DEDUCT", "订单扣减"),

    /**
     * 订单取消回滚
     */
    ORDER_CANCEL("ORDER_CANCEL", "订单取消回滚"),

    /**
     * 订单支付成功（确认库存）
     */
    ORDER_PAY("ORDER_PAY", "订单支付确认"),

    /**
     * 手动调整
     */
    MANUAL_ADJUST("MANUAL_ADJUST", "手动调整"),

    /**
     * 退货入库
     */
    RETURN_IN("RETURN_IN", "退货入库");

    /**
     * 操作类型代码
     */
    private final String code;

    /**
     * 操作类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 操作类型代码
     * @return 枚举值
     */
    public static StockOperationType getByCode(String code) {
        for (StockOperationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的库存操作类型: " + code);
    }
}
