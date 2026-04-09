package com.nexusmall.search.infrastructure.client;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.infrastructure.client.dto.ProductClientDTO;
import com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Product 服务 Feign 客户�?
 * <p>
 * API 版本号由 FeignApiVersionInterceptor 自动透传，无需手动指定
 * </p>
 */
@FeignClient(name = "nexusmall-product", fallback = ProductClientFallback.class)
public interface ProductClient {

    /**
     * 查询单个商品
     *
     * @param skuId SKU ID
     * @return 商品信息
     */
    @GetMapping("/products/{skuId}")
    Result<ProductClientDTO> getProduct(@PathVariable("skuId") Long skuId);

    /**
     * 搜索商品列表
     *
     * @param request 查询请求
     * @return 商品列表
     */
    @GetMapping("/products/search")
    Result<List<ProductClientDTO>> searchProducts(@SpringQueryMap ProductQueryClientRequest request);
}
