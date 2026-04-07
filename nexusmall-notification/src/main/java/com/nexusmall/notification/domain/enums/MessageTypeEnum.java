package com.nexusmall.notification.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    /** 系统通知 */
    SYSTEM(1, "系统通知"),

    /** 订单状态 */
    ORDER(2, "订单状态"),

    /** 营销活动 */
    PROMOTION(3, "营销活动"),

    /** 优惠券提醒 */
    COUPON(4, "优惠券提醒");

    private final Integer code;
    private final String description;

    public static MessageTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
