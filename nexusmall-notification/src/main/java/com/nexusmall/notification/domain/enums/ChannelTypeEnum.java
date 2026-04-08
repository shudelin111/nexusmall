package com.nexusmall.notification.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知渠道枚举
 * <p>
 * 业界标准：
 * - 支持多渠道通知
 * - 枚举化管理
 * - 便于扩展
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum ChannelTypeEnum {

    /** 短信 */
    SMS(1, "短信"),

    /** 邮件 */
    EMAIL(2, "邮件"),

    /** APP推送 */
    APP_PUSH(3, "APP推送"),

    /** 微信小程序 */
    WECHAT_MINI_PROGRAM(4, "微信小程序"),

    /** 微信公众号 */
    WECHAT_OFFICIAL(5, "微信公众号"),

    /** 站内信 */
    INBOX(6, "站内信");

    /**
     * 渠道代码
     */
    private final Integer code;

    /**
     * 渠道描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 渠道代码
     * @return 枚举值
     */
    public static ChannelTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChannelTypeEnum channel : values()) {
            if (channel.getCode().equals(code)) {
                return channel;
            }
        }
        return null;
    }
}
