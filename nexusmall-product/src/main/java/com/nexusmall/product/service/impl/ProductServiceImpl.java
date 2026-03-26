package com.nexusmall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nexusmall.common.annotation.DistributedLock;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.ProductException;
import com.nexusmall.product.dao.ProductMapper;
import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.exception.ProductNotFoundException;
import com.nexusmall.product.service.ProductService;
import com.nexusmall.product.service.RocketMQProducer;
import com.nexusmall.product.vo.ProductQueryRequest;
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
        log.debug("查询所有商品列表");
        List<Product> products = productMapper.list();
        log.info("查询到{}条商品数据", products.size());
        return products;
    }

    @Override
    public Product getBySkuId(Long skuId) {
        log.debug("根据 SKU ID 查询商品，skuId: {}", skuId);
        Product product = productMapper.selectById(skuId);
        if (product == null) {
            log.warn("商品不存在，skuId: {}", skuId);
            throw new ProductNotFoundException(skuId);
        }
        log.info("商品查询成功，skuId: {}, productName: {}", skuId, product.getSkuName());
        return product;
    }

    @Override
    public List<ProductVO> listByCondition(ProductQueryRequest request) {
        log.info("条件查询商品，keyword: {}, categoryId: {}, brandId: {}, status: {}", 
                request.getKeyword(), request.getCategoryId(), request.getBrandId(), request.getStatus());
        List<Product> products = productMapper.listByCondition(
                request.getKeyword(), 
                request.getCategoryId(), 
                request.getBrandId(), 
                request.getStatus());
        List<ProductVO> result = products.stream().map(p -> BeanUtil.copyProperties(p, ProductVO.class)).collect(Collectors.toList());
        log.info("查询到{}条商品数据", result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(ProductVO productVO) {
        log.info("保存商品，productName: {}, categoryId: {}", productVO.getSkuName(), productVO.getCategoryId());
        Product product = BeanUtil.copyProperties(productVO, Product.class);
        product.setStatus(1); // 默认上架
        int result = productMapper.insert(product);
        log.info("商品保存{}，skuId: {}, result: {}", result > 0 ? "成功" : "失败", product.getSkuId(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ProductVO productVO) {
        log.info("更新商品，skuId: {}, productName: {}", productVO.getSkuId(), productVO.getSkuName());
        Product product = BeanUtil.copyProperties(productVO, Product.class);
        int result = productMapper.updateById(product);
        log.info("商品更新{}，skuId: {}, result: {}", result > 0 ? "成功" : "失败", productVO.getSkuId(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long skuId) {
        log.info("删除商品，skuId: {}", skuId);
        int result = productMapper.deleteById(skuId);
        log.info("商品删除{}，skuId: {}, result: {}", result > 0 ? "成功" : "失败", skuId, result);
        return result;
    }

    @Override
    @DistributedLock(key = "'stock:decrease:' + #skuId", waitTime = 5, leaseTime = 30)
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
            throw new ProductException(CommonResultCode.INSUFFICIENT_STOCK.getCode(), CommonResultCode.INSUFFICIENT_STOCK.getMessage());
        }
    }

    @Override
    @DistributedLock(key = "'stock:increase:' + #skuId", waitTime = 5, leaseTime = 30)
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long skuId, Integer count) {
        log.info("开始增加库存，skuId: {}, count: {}", skuId, count);
        int result = productMapper.increaseStock(skuId, count);
        if (result > 0) {
            log.info("库存增加成功，skuId: {}, count: {}", skuId, count);
            return true;
        } else {
            log.error("库存增加失败，skuId: {}, count: {}", skuId, count);
            throw new ProductException(CommonResultCode.STOCK_OPERATION_FAILED.getCode(), CommonResultCode.STOCK_OPERATION_FAILED.getMessage());
        }
    }

    @Override
    public boolean checkStock(Long skuId, Integer count) {
        log.debug("检查库存，skuId: {}, count: {}", skuId, count);
        boolean result = productMapper.checkStock(skuId, count);
        log.info("库存检查结果：skuId: {}, available: {}", skuId, result);
        return result;
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
    @DistributedLock(key = "'stock:batch:decrease'", waitTime = 10, leaseTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDecreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("开始批量扣减库存，stockDTOS: {}", stockDTOS);
        int result = productMapper.batchDecreaseStock(stockDTOS);
        if (result > 0) {
            log.info("批量扣减库存成功");
            return true;
        } else {
            log.error("批量扣减库存失败");
            throw new ProductException(CommonResultCode.STOCK_OPERATION_FAILED.getCode(), CommonResultCode.STOCK_OPERATION_FAILED.getMessage());
        }
    }

    @Override
    @DistributedLock(key = "'stock:batch:increase'", waitTime = 10, leaseTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncreaseStock(List<ProductStockDTO> stockDTOS) {
        log.info("开始批量增加库存，stockDTOS: {}", stockDTOS);
        int result = productMapper.batchIncreaseStock(stockDTOS);
        if (result > 0) {
            log.info("批量增加库存成功");
            return true;
        } else {
            log.error("批量增加库存失败");
            throw new ProductException(CommonResultCode.STOCK_OPERATION_FAILED.getCode(), CommonResultCode.STOCK_OPERATION_FAILED.getMessage());
        }
    }
}
