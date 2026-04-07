package com.nexusmall.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体
 * <p>
 * 数据库映射：notification_message 表
 * 业务说明：
 * - 存储用户的站内通知消息
 * - 支持多种消息类型（系统通知、订单状态、营销活动、优惠券）
 * - 提供未读/已读状态管理
 * - 通过业务类型和业务ID关联具体业务
 * </p>
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
    /** 主键ID，自增策略 */
    private Long id;

    @Schema(description = "会员ID")
    /** 接收消息的会员ID，关联 member 表 */
    private Long memberId;

    @Schema(description = "消息标题")
    /** 消息标题，最多200字符 */
    private String title;

    @Schema(description = "消息内容")
    /** 消息正文内容，支持换行符 */
    private String content;

    @Schema(description = "消息类型：1=系统通知，2=订单状态，3=营销活动，4=优惠券提醒")
    /**
     * 消息类型枚举：
     * 1 = 系统通知（欢迎消息、平台公告）
     * 2 = 订单状态（支付成功、发货通知）
     * 3 = 营销活动（秒杀提醒、促销通知）
     * 4 = 优惠券提醒（到账通知、即将过期）
     */
    private Integer type;

    @Schema(description = "阅读状态：0=未读，1=已读")
    /** 阅读状态：0=未读（默认），1=已读 */
    private Integer status;

    @Schema(description = "业务类型：ORDER/PROMOTION/COUPON")
    /**
     * 业务类型标识：
     * - ORDER：订单相关业务
     * - PROMOTION：促销活动相关业务
     * - COUPON：优惠券相关业务
     * - USER：用户相关业务
     */
    private String businessType;

    @Schema(description = "业务ID（如订单ID/活动ID）")
    /** 关联的业务实体ID，如订单ID、活动ID、优惠券ID等 */
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
