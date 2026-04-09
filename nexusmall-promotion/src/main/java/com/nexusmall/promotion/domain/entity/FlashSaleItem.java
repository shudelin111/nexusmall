package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体
 * <p>
 * 业界标准：
 * - 支持独立秒杀活动
 * - 支持库存隔离（不影响正常库存?
 * - 支持每人限购
 * - 支持排序
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("promotion_flash_sale_item")
@Schema(description = "秒杀商品实体")
public class FlashSaleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long flashSaleId;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID")
    private Long skuId;

    /**
     * 商品名称（冗余字段，方便查询?
     */
    @Schema(description = "商品名称")
    private String productName;

    /**
     * 商品图片（冗余字段）
     */
    @Schema(description = "商品图片URL")
    private String productImage;

    /**
     * 原价
     */
    @Schema(description = "原价")
    private BigDecimal originalPrice;

    /**
     * 秒杀价
     */
    @Schema(description = "秒杀价")
    private BigDecimal flashPrice;

    /**
     * 秒杀库存（独立于正常库存）
     */
    @Schema(description = "秒杀库存")
    private Integer stock;

    /**
     * 已售数量
     */
    @Schema(description = "已售数量")
    private Integer soldCount;

    /**
     * 每人限购数量?表示不限制）
     */
    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    /**
     * 排序（数字越小越靠前?
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @Schema(description = "版本号")
    private Integer version;

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
