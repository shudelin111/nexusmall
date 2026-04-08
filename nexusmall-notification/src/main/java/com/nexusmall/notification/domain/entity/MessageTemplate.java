package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息模板实体
 * <p>
 * 业界标准：
 * - 模板与渠道绑定
 * - 支持占位符动态渲染
 * - 支持启用/禁用控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("message_template")
@Schema(description = "消息模板实体")
public class MessageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板代码（唯一标识）")
    private String templateCode;

    @Schema(description = "渠道：1=短信，2=邮件，3=APP Push，4=微信")
    private Integer channel;

    @Schema(description = "模板内容（支持占位符）")
    private String templateContent;

    @Schema(description = "变量列表（JSON格式）")
    private String variables;

    @Schema(description = "状态：0=禁用，1=启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
