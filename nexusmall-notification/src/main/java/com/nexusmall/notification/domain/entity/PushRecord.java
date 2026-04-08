package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送通知记录实体（APP Push/微信模板消息）
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("push_notification_record")
@Schema(description = "推送通知记录实体")
public class PushRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "推送平台：1=APP Push，2=微信小程序，3=微信公众号")
    private Integer platform;

    @Schema(description = "推送标题")
    private String title;

    @Schema(description = "推送内容")
    private String content;

    @Schema(description = "扩展数据（JSON格式，用于跳转链接等）")
    private String extraData;

    @Schema(description = "推送状态：0=待推送，1=推送成功，2=推送失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "业务ID（用于幂等性控制）")
    private String bizId;

    @Schema(description = "推送时间")
    private LocalDateTime pushTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
