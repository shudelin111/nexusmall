package com.nexusmall.search.infrastructure.client;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.ProductDataRepository;
import com.nexusmall.search.infrastructure.client.dto.ProductClientDTO;
import com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductDataRepositoryImpl implements ProductDataRepository {

    private static final String API_VERSION = "v1";

    private final ProductClient productClient;

    public ProductDataRepositoryImpl(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public ProductIndexDocument findByProductId(Long productId) {
        Result<ProductClientDTO> result = productClient.getProduct(productId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return null;
        }
        return toIndexDocument(result.getData());
    }

    @Override
    public List<ProductIndexDocument> listOnSaleProducts() {
        ProductQueryClientRequest request = new ProductQueryClientRequest();
        request.setStatus(1);
        Result<List<ProductClientDTO>> result = productClient.searchProducts(request);
        if (result == null || !result.isSuccess() || CollectionUtils.isEmpty(result.getData())) {
            return new ArrayList<ProductIndexDocument>();
        }
        List<ProductIndexDocument> documents = new ArrayList<ProductIndexDocument>();
        for (ProductClientDTO product : result.getData()) {
            documents.add(toIndexDocument(product));
        }
        return documents;
    }

    private ProductIndexDocument toIndexDocument(ProductClientDTO product) {
        ProductIndexDocument document = new ProductIndexDocument();
        document.setProductId(product.getSkuId());
        document.setName(product.getSkuName());
        document.setSubTitle(product.getDescription());
        document.setCategoryId(product.getCategoryId());
        document.setCategoryName(product.getCategoryName());
        document.setBrandId(product.getBrandId());
        document.setBrandName(product.getBrandName());
        document.setPrice(product.getPrice());
        document.setStock(product.getStock());
        document.setStatus(product.getStatus());
        document.setMainImage(null);
        List<String> attrs = new ArrayList<String>();
        if (product.getBrandName() != null) {
            attrs.add(product.getBrandName());
        }
        if (product.getCategoryName() != null) {
            attrs.add(product.getCategoryName());
        }
        document.setSearchableAttributes(attrs);
        return document;
    }
}
