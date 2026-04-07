package com.nexusmall.search.application.service;

import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.ProductDataRepository;
import com.nexusmall.search.domain.index.service.IndexDomainService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndexApplicationService {

    private final IndexDomainService indexDomainService;
    private final SearchAssembler searchAssembler;
    private final ProductDataRepository productDataRepository;

    public IndexApplicationService(IndexDomainService indexDomainService,
                                   SearchAssembler searchAssembler,
                                   ProductDataRepository productDataRepository) {
        this.indexDomainService = indexDomainService;
        this.searchAssembler = searchAssembler;
        this.productDataRepository = productDataRepository;
    }

    public void buildProductIndex(BuildProductIndexCommand command) {
        ProductIndexDocument document = command.getName() == null
                ? productDataRepository.findByProductId(command.getProductId())
                : searchAssembler.toIndexDocument(command);
        if (document == null) {
            throw new IllegalArgumentException("Product not found for indexing");
        }
        indexDomainService.save(document);
    }

    public void removeProductIndex(RemoveProductIndexCommand command) {
        indexDomainService.remove(command.getProductId());
    }

    public int rebuildIndex(RebuildIndexCommand command) {
        IndexBuildTask task = new IndexBuildTask();
        task.setFullRebuild(command.isFullRebuild());
        task.setScheduledAt(LocalDateTime.now());
        List<ProductIndexDocument> documents = new ArrayList<ProductIndexDocument>();
        if (command.getProducts() == null || command.getProducts().isEmpty()) {
            documents.addAll(productDataRepository.listOnSaleProducts());
        } else {
            for (BuildProductIndexCommand item : command.getProducts()) {
                documents.add(item.getName() == null
                        ? productDataRepository.findByProductId(item.getProductId())
                        : searchAssembler.toIndexDocument(item));
            }
        }
        return indexDomainService.rebuild(task, documents);
    }

    public void syncProductIndex(Long productId) {
        ProductIndexDocument document = productDataRepository.findByProductId(productId);
        if (document != null) {
            indexDomainService.save(document);
        } else {
            indexDomainService.remove(productId);
        }
    }
}
