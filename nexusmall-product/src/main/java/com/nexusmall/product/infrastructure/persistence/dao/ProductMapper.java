package com.nexusmall.product.infrastructure.persistence.dao;

import com.nexusmall.product.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 根据 ID 查询商品
     */
    Product selectById(@Param("skuId") Long skuId);

    /**
     * 查询所有商品
     */
    List<Product> list();

    /**
     * 根据条件查询商品列表
     */
    List<Product> listByCondition(@Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("brandId") Long brandId,
                                   @Param("status") Integer status);

    /**
     * 新增商品
     */
    int insert(Product product);

    /**
     * 更新商品
     */
    int updateById(Product product);

    /**
     * 删除商品
     */
    int deleteById(@Param("skuId") Long skuId);

    /**
     * 扣减库存
     */
    int decreaseStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * 增加库存
     */
    int increaseStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * 批量扣减库存
     */
    int batchDecreaseStock(@Param("list") List<ProductStockDTO> stockDTOS);

    /**
     * 批量增加库存
     */
    int batchIncreaseStock(@Param("list") List<ProductStockDTO> stockDTOS);

    /**
     * 检查库存是否充足
     */
    boolean checkStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * 上架商品
     */
    int putOnSale(@Param("skuId") Long skuId);

    /**
     * 下架商品
     */
    int putOffSale(@Param("skuId") Long skuId);

    /**
     * 根据 SKU ID 列表查询商品
     */
    List<Product> listBySkuIds(@Param("skuIds") List<Long> skuIds);
}
