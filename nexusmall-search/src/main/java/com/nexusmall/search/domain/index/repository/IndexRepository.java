package com.nexusmall.search.domain.index.repository;

import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;

import java.util.List;

public interface IndexRepository {

    void save(ProductIndexDocument document);

    void deleteByProductId(Long productId);

    int rebuild(IndexBuildTask task, List<ProductIndexDocument> documents);
}
