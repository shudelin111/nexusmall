package com.nexusmall.search.domain.index.service;

import com.nexusmall.search.config.SearchProperties;
import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.IndexRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class IndexDomainService {

    private final IndexRepository indexRepository;
    private final SearchProperties searchProperties;

    public IndexDomainService(IndexRepository indexRepository, SearchProperties searchProperties) {
        this.indexRepository = indexRepository;
        this.searchProperties = searchProperties;
    }

    public void save(ProductIndexDocument document) {
        indexRepository.save(document);
    }

    public void remove(Long productId) {
        indexRepository.deleteByProductId(productId);
    }

    public int rebuild(IndexBuildTask task, List<ProductIndexDocument> documents) {
        if (task.getIndexAlias() == null) {
            task.setIndexAlias(searchProperties.getIndexAlias());
        }
        if (documents == null) {
            documents = Collections.emptyList();
        }
        return indexRepository.rebuild(task, documents);
    }
}
