package com.nexusmall.member.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分变化历史记录实体�?
 * <p>
 * 业界标准�?
 * - 记录每次积分变化的详细信�?
 * - 支持审计和追�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_integration_change_history")
public class IntegrationChangeHistory {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * 变化类型�?=增加�?=减少
     */
    private Integer changeType;

    /**
     * 变化数量
     */
    private Integer changeCount;

    /**
     * 来源类型：ORDER(订单)/COUPON(优惠�?/REFUND(退�?
     */
    private String sourceType;

    /**
     * 来源 ID
     */
    private Long sourceId;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
