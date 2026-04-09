package com.nexusmall.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU库存实体类
 * <p>
 * 对应数据库表：inventory_sku_stock
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("inventory_sku_stock")
public class SkuStock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 锁定库存数量（下单后锁指定
     */
    private Integer lockedStock;

    /**
     * 实际库存 = 可用库存 + 锁定库存
     */
    private Integer actualStock;

    /**
     * 库存预警阈值
     */
    private Integer warningThreshold;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
