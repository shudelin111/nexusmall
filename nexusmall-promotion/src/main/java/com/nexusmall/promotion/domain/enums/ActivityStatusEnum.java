package com.nexusmall.promotion.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum ActivityStatusEnum {

    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),

    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),

    /**
     * 已结束
     */
    ENDED(2, "已结束"),

    /**
     * 已下架
     */
    OFF_SHELF(3, "已下架");

    private final Integer code;
    private final String desc;

    public static ActivityStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ActivityStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
