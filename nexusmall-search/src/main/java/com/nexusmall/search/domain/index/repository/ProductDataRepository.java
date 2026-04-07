package com.nexusmall.search.domain.index.repository;

import com.nexusmall.search.domain.index.model.ProductIndexDocument;

import java.util.List;

public interface ProductDataRepository {

    ProductIndexDocument findByProductId(Long productId);

    List<ProductIndexDocument> listOnSaleProducts();
}
