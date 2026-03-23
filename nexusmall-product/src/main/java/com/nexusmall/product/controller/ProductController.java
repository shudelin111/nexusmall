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

/**
 * Controller for handling product-related requests.
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * Health check endpoint to verify the service status.
     * Also performs a quick Redis write/read check to ensure connectivity.
     *
     * @return Result containing service status and message.
     */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("service", "nexusmall-product");
        payload.put("status", "UP");
        payload.put("message", "product service is ready");

        // Verify redis connectivity
        String testKey = "ping:test";
        redisUtils.set(testKey, "ok");
        payload.put("redis", redisUtils.get(testKey));

        return Result.success(payload);
    }

    /**
     * Retrieves a list of all products.
     *
     * @return Result containing a list of Product entities.
     */
    @GetMapping("/list")
    public Result<List<Product>> listProducts() {
        return Result.success(productService.listProducts());
    }

    /**
     * Retrieves a specific product by its SKU ID.
     *
     * @param skuId The unique identifier for the stock keeping unit.
     * @return Result containing the requested Product.
     */
    @GetMapping("/{skuId}")
    public Result<Product> getProduct(@PathVariable Long skuId) {
        return Result.success(productService.getBySkuId(skuId));
    }

    /**
     * Debug endpoint to test Redis caching for a specific product.
     * Stores the product in Redis and immediately retrieves it to verify serialization.
     *
     * @param skuId The SKU ID of the product to test.
     * @return Result containing debug information about the cache operation.
     */
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
