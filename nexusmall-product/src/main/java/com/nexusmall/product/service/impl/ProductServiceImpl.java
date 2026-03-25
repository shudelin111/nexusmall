package com.nexusmall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.nexusmall.product.dao.ProductMapper;
import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.exception.ProductNotFoundException;
import com.nexusmall.product.service.ProductService;
import com.nexusmall.product.service.RocketMQProducer;
import com.nexusmall.product.vo.ProductVO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RocketMQProducer rocketMQProducer;

    @Override
    public List<Product> listProducts() {
        return productMapper.list();
    }

    @Override
    public Product getBySkuId(Long skuId) {
        Product product = productMapper.selectById(skuId);
        if (product == null) {
            throw new ProductNotFoundException(skuId);
        }
        return product;
    }

    @Override
    public List<ProductVO> listByCondition(String keyword, Long categoryId, Long brandId, Integer status) {
        List<Product> products = productMapper.listByCondition(keyword, categoryId, brandId, status);
        return products.stream().map(p -> BeanUtil.copyProperties(p, ProductVO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(ProductVO productVO) {
        Product product = BeanUtil.copyProperties(productVO, Product.class);
        product.setStatus(1); // 默认上架
        return productMapper.insert(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ProductVO productVO) {
        Product product = BeanUtil.copyProperties(productVO, Product.class);
        return productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long skuId) {
        return productMapper.deleteById(skuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long skuId, Integer count) {
        log.info("开始扣减库存，skuId: {}, count: {}", skuId, count);
        int result = productMapper.decreaseStock(skuId, count);
        if (result > 0) {
            log.info("库存扣减成功，skuId: {}, count: {}", skuId, count);
            
            // 发送库存扣减成功消息
            rocketMQProducer.sendStockDecreasedMessage(skuId, count);
            
            return true;
        } else {
            log.error("库存扣减失败，skuId: {}, count: {}，库存不足", skuId, count);
            throw new RuntimeException("库存不足");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long skuId, Integer count) {
        log.info("开始增加库存，skuId: {}, count: {}", skuId, count);
        int result = productMapper.increaseStock(skuId, count);
        if (result > 0) {
            log.info("库存增加成功，skuId: {}, count: {}", skuId, count);
            return true;
        } else {
            log.error("库存增加失败，skuId: {}, count: {}", skuId, count);
            throw new RuntimeException("库存增加失败");
        }
    }

    @Override
    public boolean checkStock(Long skuId, Integer count) {
        return productMapper.checkStock(skuId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean putOnSale(Long skuId) {
        int result = productMapper.putOnSale(skuId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean putOffSale(Long skuId) {
        int result = productMapper.putOffSale(skuId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDecreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("开始批量扣减库存，stockDTOS: {}", stockDTOS);
        int result = productMapper.batchDecreaseStock(stockDTOS);
        if (result > 0) {
            log.info("批量扣减库存成功");
            return true;
        } else {
            log.error("批量扣减库存失败");
            throw new RuntimeException("批量扣减库存失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("开始批量增加库存，stockDTOS: {}", stockDTOS);
        int result = productMapper.batchIncreaseStock(stockDTOS);
        if (result > 0) {
            log.info("批量增加库存成功");
            return true;
        } else {
            log.error("批量增加库存失败");
            throw new RuntimeException("批量增加库存失败");
        }
    }
}
