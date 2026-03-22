package com.nexusmall.product.controller;

import com.nexusmall.common.util.RedisUtils;
import com.nexusmall.common.vo.Result;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisUtils redisUtils;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("service", "nexusmall-product");
        payload.put("status", "UP");
        payload.put("message", "product service is ready");
        redisUtils.set("kkk", "vvv");
        System.out.println(redisUtils.get("kkk"));
        return Result.success(payload);
    }

    @GetMapping("/list")
    public Result<List<Product>> listProducts() {
        return Result.success(productService.listProducts());
    }

    @GetMapping("/{skuId}")
    public Result<Product> getProduct(@PathVariable Long skuId) {
        return Result.success(productService.getBySkuId(skuId));
    }

    @GetMapping("/redis/debug/{skuId}")
    public Result<Map<String, Object>> debugRedis(@PathVariable Long skuId) {
        Product product = productService.getBySkuId(skuId);
        String cacheKey = "product:debug:" + skuId;
        redisUtils.set(cacheKey, product);

        Product cachedProduct = redisUtils.get(cacheKey, Product.class);
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("key", cacheKey);
        payload.put("exists", redisUtils.hasKey(cacheKey));
        payload.put("value", cachedProduct);
        return Result.success(payload);
    }
}
