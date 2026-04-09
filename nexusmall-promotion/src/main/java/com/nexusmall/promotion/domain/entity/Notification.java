package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 * <p>
 * 业界标准�?
 * - 支持多种通知类型（站内信、短信、邮件、推送）
 * - 支持消息模板
 * - 支持已读/未读状�?
 * - 支持批量发�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("promotion_notification")
@Schema(description = "消息通知实体")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long id;

    /**
     * 用户ID�?表示全员通知�?
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 通知类型�?-优惠券到�?2-秒杀开�?3-活动提醒 4-系统公告
     */
    @Schema(description = "通知类型")
    private Integer type;

    /**
     * 通知渠道�?-站内�?2-短信 3-邮件 4-APP推�?
     */
    @Schema(description = "通知渠道")
    private Integer channel;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 跳转链接
     */
    @Schema(description = "跳转链接")
    private String linkUrl;

    /**
     * 扩展数据JSON
     */
    @Schema(description = "扩展数据JSON")
    private String extraData;

    /**
     * 状态：0-未发�?1-已发�?2-发送失�?
     */
    @Schema(description = "状�?)
    private Integer status;

    /**
     * 是否已读�?-未读 1-已读
     */
    @Schema(description = "是否已读")
    private Integer isRead;

    /**
     * 发送时�?
     */
    @Schema(description = "发送时�?)
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
