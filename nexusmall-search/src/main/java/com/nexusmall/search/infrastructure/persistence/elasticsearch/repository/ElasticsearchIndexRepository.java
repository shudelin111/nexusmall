package com.nexusmall.search.infrastructure.persistence.elasticsearch.repository;

import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.IndexRepository;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.converter.ProductDocumentConverter;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.document.ProductDocument;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.support.ProductIndexSchemaManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
public class ElasticsearchIndexRepository implements IndexRepository {

    private final RestHighLevelClient client;
    private final ProductIndexSchemaManager schemaManager;
    private final ProductDocumentConverter productDocumentConverter;
    private final ObjectMapper objectMapper;

    public ElasticsearchIndexRepository(RestHighLevelClient client,
                                        ProductIndexSchemaManager schemaManager,
                                        ProductDocumentConverter productDocumentConverter,
                                        ObjectMapper objectMapper) {
        this.client = client;
        this.schemaManager = schemaManager;
        this.productDocumentConverter = productDocumentConverter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(ProductIndexDocument document) {
        schemaManager.ensureIndex();
        ProductDocument productDocument = productDocumentConverter.toDocument(document);
        IndexRequest request = new IndexRequest(schemaManager.getIndexName())
                .id(String.valueOf(document.getProductId()))
                .source(write(productDocument), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save product index document", ex);
        }
    }

    @Override
    public void deleteByProductId(Long productId) {
        schemaManager.ensureIndex();
        DeleteRequest request = new DeleteRequest(schemaManager.getIndexName(), String.valueOf(productId));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete product index document", ex);
        }
    }

    @Override
    public int rebuild(IndexBuildTask task, List<ProductIndexDocument> documents) {
        schemaManager.recreateIndex();
        if (documents == null || documents.isEmpty()) {
            return 0;
        }
        BulkRequest request = new BulkRequest();
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (ProductIndexDocument document : documents) {
            if (document == null || document.getProductId() == null) {
                continue;
            }
            ProductDocument productDocument = productDocumentConverter.toDocument(document);
            request.add(new IndexRequest(schemaManager.getIndexName())
                    .id(String.valueOf(document.getProductId()))
                    .source(write(productDocument), XContentType.JSON));
        }
        try {
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
            if (response.hasFailures()) {
                throw new IllegalStateException("Failed to rebuild product index: " + response.buildFailureMessage());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to rebuild product index", ex);
        }
        return documents.size();
    }

    private String write(ProductDocument document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize product document", ex);
        }
    }
}
