package com.nexusmall.search.infrastructure.client;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.infrastructure.client.dto.ProductClientDTO;
import com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "nexusmall-product", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products/{skuId}")
    Result<ProductClientDTO> getProduct(@RequestHeader("X-API-Version") String apiVersion,
                                        @PathVariable("skuId") Long skuId);

    @GetMapping("/products/search")
    Result<List<ProductClientDTO>> searchProducts(@RequestHeader("X-API-Version") String apiVersion,
                                                  @SpringQueryMap ProductQueryClientRequest request);
}
