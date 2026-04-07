package com.nexusmall.product.application.service.impl;

import com.nexusmall.product.application.service.ProductService;
import com.nexusmall.product.domain.entity.Product;
import com.nexusmall.product.infrastructure.persistence.dao.ProductMapper;
import com.nexusmall.product.infrastructure.persistence.dao.ProductStockDTO;
import com.nexusmall.product.interfaces.dto.ProductQueryRequest;
import com.nexusmall.product.interfaces.dto.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public List<Product> listProducts() {
        log.info("查询所有商品");
        return productMapper.list();
    }

    @Override
    public Product getBySkuId(Long skuId) {
        log.info("根据SKU ID查询商品: skuId={}", skuId);
        return productMapper.selectById(skuId);
    }

    @Override
    public List<ProductVO> listByCondition(ProductQueryRequest request) {
        log.info("根据条件查询商品列表");
        
        List<Product> products = productMapper.listByCondition(
                request.getKeyword(),
                request.getCategoryId(),
                request.getBrandId(),
                request.getStatus()
        );
        
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int save(ProductVO productVO) {
        log.info("新增商品: skuName={}", productVO.getSkuName());
        
        Product product = convertToEntity(productVO);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setVersion(0);
        
        return productMapper.insert(product);
    }

    @Override
    public int updateById(ProductVO productVO) {
        log.info("更新商品: skuId={}", productVO.getSkuId());
        
        Product product = convertToEntity(productVO);
        product.setUpdateTime(LocalDateTime.now());
        
        return productMapper.updateById(product);
    }

    @Override
    public int deleteById(Long skuId) {
        log.info("删除商品: skuId={}", skuId);
        return productMapper.deleteById(skuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long skuId, Integer count) {
        log.info("扣减库存: skuId={}, count={}", skuId, count);
        
        int rows = productMapper.decreaseStock(skuId, count);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long skuId, Integer count) {
        log.info("增加库存: skuId={}, count={}", skuId, count);
        
        int rows = productMapper.increaseStock(skuId, count);
        return rows > 0;
    }

    @Override
    public boolean checkStock(Long skuId, Integer count) {
        log.info("检查库存: skuId={}, count={}", skuId, count);
        return productMapper.checkStock(skuId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean putOnSale(Long skuId) {
        log.info("上架商品: skuId={}", skuId);
        
        int rows = productMapper.putOnSale(skuId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean putOffSale(Long skuId) {
        log.info("下架商品: skuId={}", skuId);
        
        int rows = productMapper.putOffSale(skuId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDecreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("批量扣减库存: count={}", stockDTOS.size());
        
        for (ProductStockDTO dto : stockDTOS) {
            boolean success = decreaseStock(dto.getSkuId(), dto.getCount());
            if (!success) {
                log.error("批量扣减库存失败: skuId={}", dto.getSkuId());
                return false;
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("批量增加库存: count={}", stockDTOS.size());
        
        for (ProductStockDTO dto : stockDTOS) {
            boolean success = increaseStock(dto.getSkuId(), dto.getCount());
            if (!success) {
                log.error("批量增加库存失败: skuId={}", dto.getSkuId());
                return false;
            }
        }
        
        return true;
    }

    /**
     * Entity 转 VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    /**
     * VO 转 Entity
     */
    private Product convertToEntity(ProductVO vo) {
        Product product = new Product();
        BeanUtils.copyProperties(vo, product);
        return product;
    }
}
