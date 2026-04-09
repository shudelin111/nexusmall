package com.nexusmall.common.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息基类
 * <p>
 * 所?MQ 消息都应该继承此类，提供通用的消息属?
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Data
public abstract class BaseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID（唯一标识?
     */
    private String messageId;

    /**
     * 消息发送时?
     */
    private LocalDateTime sendTime;

    /**
     * 消息来源服务
     */
    private String sourceService;

    /**
     * 消息版本（用于兼容性）
     */
    private String version = "1.0";

    public BaseMessage() {
        this.sendTime = LocalDateTime.now();
    }

    public BaseMessage(String messageId, String sourceService) {
        this();
        this.messageId = messageId;
        this.sourceService = sourceService;
    }
}
