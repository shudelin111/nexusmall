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
 * 会员等级实体类
 * <p>
 * 业界标准：
 * - 定义会员等级体系（普通/黄金/铂金/钻石）
 * - 每个等级对应不同的权益（折扣/免运费门槛等）
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_member_level")
public class MemberLevel {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 所需成长值
     */
    private Integer growthPointThreshold;

    /**
     * 折扣率（0.95=95折）
     */
    private BigDecimal discount;

    /**
     * 免运费门槛
     */
    private BigDecimal freeShippingThreshold;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0=禁用，1=正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
