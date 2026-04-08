package com.nexusmall.promotion.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {

    /**
     * 优惠券到期提醒
     */
    COUPON_EXPIRE(1, "优惠券到期提醒"),

    /**
     * 秒杀活动开始
     */
    FLASH_SALE_START(2, "秒杀活动开始"),

    /**
     * 活动即将结束
     */
    ACTIVITY_ENDING(3, "活动即将结束"),

    /**
     * 系统公告
     */
    SYSTEM_ANNOUNCEMENT(4, "系统公告");

    private final Integer code;
    private final String desc;
}
