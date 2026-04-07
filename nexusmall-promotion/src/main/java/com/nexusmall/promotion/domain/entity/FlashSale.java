package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动实体
 * <p>
 * 业界标准：
 * - 支持活动时间段管理
 * - 支持活动状态流转
 * - 支持并发控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("promotion_flash_sale")
@Schema(description = "秒杀活动实体")
public class FlashSale implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String name;

    /**
     * 活动描述
     */
    @Schema(description = "活动描述")
    private String description;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 状态：0-未开始 1-进行中 2-已结束 3-已下架
     */
    @Schema(description = "状态：0-未开始 1-进行中 2-已结束 3-已下架")
    private Integer status;

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

    /**
     * 逻辑删除
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
