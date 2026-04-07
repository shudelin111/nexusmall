package com.nexusmall.inventory.interfaces.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU库存视图对象 VO
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
public class SkuStockVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 可用库存数量
     */
    private Integer stock;

    /**
     * 锁定库存数量
     */
    private Integer lockedStock;

    /**
     * 实际库存
     */
    private Integer actualStock;

    /**
     * 库存预警阈值
     */
    private Integer warningThreshold;

    /**
     * 是否低库存（stock <= warningThreshold）
     */
    private Boolean lowStock;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
