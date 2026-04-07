package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("notification_message")
@Schema(description = "站内消息实体")
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型：1=系统通知，2=订单状态，3=营销活动，4=优惠券提醒")
    private Integer type;

    @Schema(description = "阅读状态：0=未读，1=已读")
    private Integer status;

    @Schema(description = "业务类型：ORDER/PROMOTION/COUPON")
    private String businessType;

    @Schema(description = "业务ID（如订单ID/活动ID）")
    private Long businessId;

    @Schema(description = "逻辑删除：0=未删除，1=已删除")
    @TableLogic
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
