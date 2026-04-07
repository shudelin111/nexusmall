package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件发送记录实体
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("email_send_record")
@Schema(description = "邮件发送记录实体")
public class EmailRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "收件人邮箱")
    private String toEmail;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件内容（HTML）")
    private String content;

    @Schema(description = "邮件模板代码")
    private String templateCode;

    @Schema(description = "发送状态：0=待发送，1=发送成功，2=发送失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "业务ID（用于幂等性控制）")
    private String bizId;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
