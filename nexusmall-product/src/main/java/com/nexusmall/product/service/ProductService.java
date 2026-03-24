package com.nexusmall.product.service;

import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.vo.ProductVO;

import java.util.List;

public interface ProductService {

    /**
     * 查询所有商品
     */
    List<Product> listProducts();

    /**
     * 根据 SKU ID 查询商品
     */
    Product getBySkuId(Long skuId);

    /**
     * 根据条件查询商品列表
     */
    List<ProductVO> listByCondition(String keyword, Long categoryId, Long brandId, Integer status);

    /**
     * 新增商品
     */
    int save(ProductVO productVO);

    /**
     * 更新商品
     */
    int updateById(ProductVO productVO);

    /**
     * 删除商品
     */
    int deleteById(Long skuId);

    /**
     * 扣减库存
     */
    boolean decreaseStock(Long skuId, Integer count);

    /**
     * 增加库存
     */
    boolean increaseStock(Long skuId, Integer count);

    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long skuId, Integer count);

    /**
     * 上架商品
     */
    boolean putOnSale(Long skuId);

    /**
     * 下架商品
     */
    boolean putOffSale(Long skuId);

    /**
     * 批量扣减库存
     */
    boolean batchDecreaseStock(List<ProductStockDTO> stockDTOS);

    /**
     * 批量增加库存
     */
    boolean batchIncreaseStock(List<ProductStockDTO> stockDTOS);
}
