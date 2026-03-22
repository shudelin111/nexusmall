package com.nexusmall.product.service.impl;

import com.nexusmall.product.entity.Product;
import com.nexusmall.product.exception.ProductNotFoundException;
import com.nexusmall.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final List<Product> mockProducts = Arrays.asList(
            Product.builder()
                    .skuId(1001L)
                    .skuName("NexusMall iPhone 16 Pro")
                    .price(new BigDecimal("7999.00"))
                    .stock(25)
                    .categoryName("手机")
                    .brandName("Apple")
                    .description("用于接口联调的模拟商品")
                    .build(),
            Product.builder()
                    .skuId(1002L)
                    .skuName("NexusMall Mate 70")
                    .price(new BigDecimal("5999.00"))
                    .stock(40)
                    .categoryName("手机")
                    .brandName("Huawei")
                    .description("用于网关和订单联调的模拟商品")
                    .build(),
            Product.builder()
                    .skuId(1003L)
                    .skuName("NexusMall Redmi Book Pro")
                    .price(new BigDecimal("4999.00"))
                    .stock(18)
                    .categoryName("电脑")
                    .brandName("Xiaomi")
                    .description("用于前端商品页调试的模拟商品")
                    .build()
    );

    @Override
    public List<Product> listProducts() {
        return mockProducts;
    }

    @Override
    public Product getBySkuId(Long skuId) {
        return mockProducts.stream()
                .filter(product -> product.getSkuId().equals(skuId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(skuId));
    }
}
