package com.nexusmall.search.infrastructure.persistence.elasticsearch.converter;

import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.document.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductDocumentConverter {

    public ProductDocument toDocument(ProductIndexDocument source) {
        ProductDocument target = new ProductDocument();
        target.setProductId(source.getProductId());
        target.setName(source.getName());
        target.setSubTitle(source.getSubTitle());
        target.setCategoryId(source.getCategoryId());
        target.setCategoryName(source.getCategoryName());
        target.setBrandId(source.getBrandId());
        target.setBrandName(source.getBrandName());
        target.setPrice(source.getPrice());
        target.setMainImage(source.getMainImage());
        target.setStock(source.getStock());
        target.setStatus(source.getStatus());
        target.setSearchableAttributes(source.getSearchableAttributes());
        return target;
    }
}
