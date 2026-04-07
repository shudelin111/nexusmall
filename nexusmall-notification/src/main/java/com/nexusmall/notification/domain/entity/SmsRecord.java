package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送记录实体
 * <p>
 * 业界标准：
 * - 记录所有短信发送历史
 * - 支持幂等性控制（biz_id唯一）
 * - 便于审计和问题排查
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("sms_send_record")
@Schema(description = "短信发送记录实体")
public class SmsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "短信模板代码")
    private String templateCode;

    @Schema(description = "模板参数（JSON格式）")
    private String templateParam;

    @Schema(description = "短信内容")
    private String content;

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
