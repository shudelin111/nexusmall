package com.nexusmall.promotion.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.promotion.domain.entity.FlashSaleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface FlashSaleItemMapper extends BaseMapper<FlashSaleItem> {

    /**
     * 扣减秒杀库存（乐观锁）
     *
     * @param skuId SKU ID
     * @return 影响行数
     */
    @Update("UPDATE promotion_flash_sale_item SET stock = stock - 1, sold_count = sold_count + 1, " +
            "version = version + 1 WHERE sku_id = #{skuId} AND stock > 0")
    int decreaseStock(@Param("skuId") Long skuId);
}
