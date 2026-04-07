package com.nexusmall.search.infrastructure.client;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.infrastructure.client.dto.ProductClientDTO;
import com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public Result<ProductClientDTO> getProduct(String apiVersion, Long skuId) {
        return Result.failure("SEARCH_PRODUCT_CLIENT_FALLBACK", "Failed to load product from product service");
    }

    @Override
    public Result<List<ProductClientDTO>> searchProducts(String apiVersion, ProductQueryClientRequest request) {
        return Result.success(Collections.<ProductClientDTO>emptyList());
    }
}
