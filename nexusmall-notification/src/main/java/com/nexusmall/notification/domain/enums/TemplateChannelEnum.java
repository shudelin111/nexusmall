package com.nexusmall.notification.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息模板渠道枚举
 * <p>
 * 业界标准：
 * - 模板与渠道绑定
 * - 支持多渠道模板配置
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum TemplateChannelEnum {

    /** 短信模板 */
    SMS(1, "短信"),

    /** 邮件模板 */
    EMAIL(2, "邮件"),

    /** APP推送模板 */
    APP_PUSH(3, "APP推送"),

    /** 微信模板 */
    WECHAT(4, "微信");

    private final Integer code;
    private final String description;

    public static TemplateChannelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TemplateChannelEnum channel : values()) {
            if (channel.getCode().equals(code)) {
                return channel;
            }
        }
        return null;
    }
}
