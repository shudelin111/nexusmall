package com.nexusmall.inventory.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.inventory.domain.entity.SkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * SKU库存 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface SkuStockMapper extends BaseMapper<SkuStock> {

    /**
     * 扣减库存（乐观锁?
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE inventory_sku_stock SET stock = stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "actual_stock = actual_stock, " +
            "version = version + 1 " +
            "WHERE sku_id = #{skuId} AND warehouse_id = #{warehouseId} " +
            "AND stock >= #{quantity} AND version = #{version}")
    int deductStock(@Param("skuId") Long skuId,
                    @Param("warehouseId") Long warehouseId,
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 回滚库存（订单取消）
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 回滚数量
     * @return 影响行数
     */
    @Update("UPDATE inventory_sku_stock SET stock = stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1 " +
            "WHERE sku_id = #{skuId} AND warehouse_id = #{warehouseId} " +
            "AND locked_stock >= #{quantity} AND version = #{version}")
    int rollbackStock(@Param("skuId") Long skuId,
                      @Param("warehouseId") Long warehouseId,
                      @Param("quantity") Integer quantity,
                      @Param("version") Integer version);

    /**
     * 锁定库存转已售（订单支付成功?
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 数量
     * @return 影响行数
     */
    @Update("UPDATE inventory_sku_stock SET locked_stock = locked_stock - #{quantity}, " +
            "actual_stock = actual_stock - #{quantity}, " +
            "version = version + 1 " +
            "WHERE sku_id = #{skuId} AND warehouse_id = #{warehouseId} " +
            "AND locked_stock >= #{quantity} AND version = #{version}")
    int confirmStock(@Param("skuId") Long skuId,
                     @Param("warehouseId") Long warehouseId,
                     @Param("quantity") Integer quantity,
                     @Param("version") Integer version);
}
