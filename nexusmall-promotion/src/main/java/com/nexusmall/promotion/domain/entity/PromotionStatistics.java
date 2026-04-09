package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营销数据统计实体（按天聚合）
 * <p>
 * 业界标准：
 * - 支持多维度统计（优惠券券、秒杀、满减）
 * - 支持按天/?月聚?
 * - 支持实时统计和离线统?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("promotion_statistics")
@Schema(description = "营销数据统计实体")
public class PromotionStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "统计ID")
    private Long id;

    /**
     * 统计日期
     */
    @Schema(description = "统计日期")
    private LocalDate statDate;

    /**
     * 统计类型?-优惠券2-秒杀 3-满减 4-综合
     */
    @Schema(description = "统计类型")
    private Integer statType;

    /**
     * 活动ID?表示汇总）
     */
    @Schema(description = "活动ID")
    private Long activityId;

    /**
     * 曝光次数
     */
    @Schema(description = "曝光次数")
    private Long impressionCount;

    /**
     * 点击次数
     */
    @Schema(description = "点击次数")
    private Long clickCount;

    /**
     * 领取数量（优惠券券
     */
    @Schema(description = "领取数量")
    private Integer receiveCount;

    /**
     * 使用数量
     */
    @Schema(description = "使用数量")
    private Integer useCount;

    /**
     * 下单数量
     */
    @Schema(description = "下单数量")
    private Integer orderCount;

    /**
     * 成交金额
     */
    @Schema(description = "成交金额")
    private BigDecimal gmv;

    /**
     * 优惠券金额
     */
    @Schema(description = "优惠券金额")
    private BigDecimal discountAmount;

    /**
     * 转化率（点击/曝光）
     */
    @Schema(description = "转化率")
    private BigDecimal clickRate;

    /**
     * 领取率（领取/点击）
     */
    @Schema(description = "领取率")
    private BigDecimal receiveRate;

    /**
     * 使用率（使用/领取）
     */
    @Schema(description = "使用率")
    private BigDecimal useRate;

    /**
     * ROI（成交金额/优惠券金额
     */
    @Schema(description = "ROI")
    private BigDecimal roi;

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
}
