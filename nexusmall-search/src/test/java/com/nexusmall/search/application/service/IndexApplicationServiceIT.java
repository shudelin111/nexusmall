package com.nexusmall.search.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.config.SearchProperties;
import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.ProductDataRepository;
import com.nexusmall.search.domain.index.service.IndexDomainService;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.converter.ProductDocumentConverter;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.repository.ElasticsearchIndexRepository;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.repository.ElasticsearchSearchRepository;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.support.ProductIndexSchemaManager;
import com.nexusmall.search.shared.enums.SearchSortType;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class IndexApplicationServiceIT {

    private ElasticsearchContainer container;
    private RestHighLevelClient client;

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void shouldRebuildAndSyncIndexThroughApplicationService() {
        Assumptions.assumeTrue(isIntegrationTestEnabled() && isDockerAvailable(),
                "Docker and -Pit are required for Elasticsearch integration tests");

        startContainer();
        SearchProperties properties = new SearchProperties();
        properties.setIndexAlias("nexusmall_search_app_it");
        properties.setIndexShards(1);
        properties.setIndexReplicas(0);

        ProductIndexSchemaManager schemaManager = new ProductIndexSchemaManager(client, properties);
        ElasticsearchIndexRepository indexRepository = new ElasticsearchIndexRepository(
                client, schemaManager, new ProductDocumentConverter(), new ObjectMapper());
        ElasticsearchSearchRepository searchRepository = new ElasticsearchSearchRepository(client, schemaManager);

        InMemoryProductDataRepository productDataRepository = new InMemoryProductDataRepository();
        productDataRepository.setOnSaleProducts(Arrays.asList(
                product(3001L, "Gaming Laptop", "RTX powered laptop", "Laptops", "NexusPro", new BigDecimal("8999.00")),
                product(3002L, "Office Monitor", "27 inch monitor", "Displays", "VisionX", new BigDecimal("1299.00"))
        ));
        productDataRepository.setSingleProduct(product(3003L, "Laptop Stand", "Aluminum stand", "Accessories", "DeskLab", new BigDecimal("199.00")));

        IndexApplicationService service = new IndexApplicationService(
                new IndexDomainService(indexRepository, properties),
                new SearchAssembler(),
                productDataRepository
        );

        RebuildIndexCommand rebuild = new RebuildIndexCommand();
        rebuild.setFullRebuild(true);
        int rebuilt = service.rebuildIndex(rebuild);
        Assertions.assertEquals(2, rebuilt);

        SearchRequest rebuildQuery = new SearchRequest();
        rebuildQuery.setKeyword("laptop");
        rebuildQuery.setPageNo(1);
        rebuildQuery.setPageSize(10);
        rebuildQuery.setSortType(SearchSortType.RELEVANCE);
        SearchResult rebuiltResult = searchRepository.search(rebuildQuery);
        Assertions.assertEquals(1L, rebuiltResult.getPage().getTotal());

        service.syncProductIndex(3003L);

        SearchRequest syncQuery = new SearchRequest();
        syncQuery.setKeyword("stand");
        syncQuery.setPageNo(1);
        syncQuery.setPageSize(10);
        syncQuery.setSortType(SearchSortType.RELEVANCE);
        SearchResult syncResult = searchRepository.search(syncQuery);
        Assertions.assertEquals(1L, syncResult.getPage().getTotal());
        Assertions.assertEquals("Laptop Stand", syncResult.getPage().getRecords().get(0).getName());
    }

    private void startContainer() {
        container = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.13"))
                .withEnv("xpack.security.enabled", "false");
        container.start();
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create(container.getHttpHostAddress())));
    }

    private ProductIndexDocument product(Long id,
                                         String name,
                                         String subTitle,
                                         String categoryName,
                                         String brandName,
                                         BigDecimal price) {
        ProductIndexDocument document = new ProductIndexDocument();
        document.setProductId(id);
        document.setName(name);
        document.setSubTitle(subTitle);
        document.setCategoryId(1L);
        document.setCategoryName(categoryName);
        document.setBrandId(1L);
        document.setBrandName(brandName);
        document.setPrice(price);
        document.setStatus(1);
        document.setStock(100);
        document.setSearchableAttributes(Arrays.asList(categoryName, brandName, subTitle));
        return document;
    }

    private boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    private boolean isIntegrationTestEnabled() {
        return Boolean.parseBoolean(System.getProperty("nexusmall.it.enabled", "false"));
    }

    private static class InMemoryProductDataRepository implements ProductDataRepository {

        private List<ProductIndexDocument> onSaleProducts = Collections.emptyList();
        private ProductIndexDocument singleProduct;

        @Override
        public ProductIndexDocument findByProductId(Long productId) {
            return singleProduct != null && singleProduct.getProductId().equals(productId) ? singleProduct : null;
        }

        @Override
        public List<ProductIndexDocument> listOnSaleProducts() {
            return onSaleProducts;
        }

        public void setOnSaleProducts(List<ProductIndexDocument> onSaleProducts) {
            this.onSaleProducts = onSaleProducts;
        }

        public void setSingleProduct(ProductIndexDocument singleProduct) {
            this.singleProduct = singleProduct;
        }
    }
}
