package com.nexusmall.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 * <p>
 * 业界标准：
 * - 记录每次积分兑换的详细信息
 * - 支持兑换商品/优惠券/现金等
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_integration_consume_record")
public class IntegrationConsumeRecord {

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
     * 消耗积分数量
     */
    private Integer integration;

    /**
     * 兑换类型：COUPON(优惠券)/PRODUCT(商品)/CASH(现金)
     */
    private String consumeType;

    /**
     * 兑换对象 ID（如优惠券ID/商品ID）
     */
    private Long objectId;

    /**
     * 兑换对象名称
     */
    private String objectName;

    /**
     * 抵扣金额（如果是现金兑换）
     */
    private BigDecimal amount;

    /**
     * 状态：0=待处理，1=已完成，2=已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
