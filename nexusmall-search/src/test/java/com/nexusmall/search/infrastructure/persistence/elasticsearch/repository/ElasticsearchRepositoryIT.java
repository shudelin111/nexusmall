package com.nexusmall.search.infrastructure.persistence.elasticsearch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.search.config.SearchProperties;
import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.suggest.model.SuggestItem;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.converter.ProductDocumentConverter;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class ElasticsearchRepositoryIT {

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
    void shouldIndexAndSearchProducts() {
        Assumptions.assumeTrue(isIntegrationTestEnabled() && isDockerAvailable(),
                "Docker and -Pit are required for Elasticsearch integration tests");

        startContainer();
        SearchProperties properties = searchProperties("nexusmall_search_repo_it");
        ProductIndexSchemaManager schemaManager = new ProductIndexSchemaManager(client, properties);
        ElasticsearchIndexRepository indexRepository = new ElasticsearchIndexRepository(
                client, schemaManager, new ProductDocumentConverter(), new ObjectMapper());
        ElasticsearchSearchRepository searchRepository = new ElasticsearchSearchRepository(client, schemaManager);

        indexRepository.rebuild(rebuildTask(), Arrays.asList(
                product(1001L, "Mechanical Keyboard", "Hot swap gaming keyboard", "Keyboards", "NexusGear", new BigDecimal("399.00")),
                product(1002L, "Wireless Mouse", "Lightweight office mouse", "Mice", "NexusGear", new BigDecimal("199.00"))
        ));

        SearchRequest request = new SearchRequest();
        request.setKeyword("keyboard");
        request.setPageNo(1);
        request.setPageSize(10);
        request.setSortType(SearchSortType.RELEVANCE);

        SearchResult result = searchRepository.search(request);

        Assertions.assertEquals(1L, result.getPage().getTotal());
        Assertions.assertEquals("Mechanical Keyboard", result.getPage().getRecords().get(0).getName());
        Assertions.assertFalse(result.getPage().getRecords().get(0).getHighlightSnippets().isEmpty());
        Assertions.assertFalse(result.getFacets().isEmpty());
    }

    @Test
    void shouldReturnSuggestionsFromIndexedDocuments() {
        Assumptions.assumeTrue(isIntegrationTestEnabled() && isDockerAvailable(),
                "Docker and -Pit are required for Elasticsearch integration tests");

        startContainer();
        SearchProperties properties = searchProperties("nexusmall_search_suggest_it");
        ProductIndexSchemaManager schemaManager = new ProductIndexSchemaManager(client, properties);
        ElasticsearchIndexRepository indexRepository = new ElasticsearchIndexRepository(
                client, schemaManager, new ProductDocumentConverter(), new ObjectMapper());
        ElasticsearchSuggestRepository suggestRepository = new ElasticsearchSuggestRepository(client, schemaManager);

        indexRepository.rebuild(rebuildTask(), Arrays.asList(
                product(2001L, "Mechanical Keyboard", "Compact keyboard", "Keyboards", "NexusGear", new BigDecimal("399.00")),
                product(2002L, "Keycap Set", "PBT keycap set", "Accessories", "NexusGear", new BigDecimal("99.00"))
        ));

        List<SuggestItem> suggestions = suggestRepository.suggest("key", 5);

        Assertions.assertFalse(suggestions.isEmpty());
        Assertions.assertTrue(suggestions.stream().anyMatch(item -> item.getKeyword().contains("Key")));
    }

    private void startContainer() {
        container = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.13"))
                .withEnv("xpack.security.enabled", "false");
        container.start();
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create(container.getHttpHostAddress())));
    }

    private SearchProperties searchProperties(String indexAlias) {
        SearchProperties properties = new SearchProperties();
        properties.setIndexAlias(indexAlias);
        properties.setIndexShards(1);
        properties.setIndexReplicas(0);
        return properties;
    }

    private IndexBuildTask rebuildTask() {
        IndexBuildTask task = new IndexBuildTask();
        task.setFullRebuild(true);
        task.setScheduledAt(LocalDateTime.now());
        return task;
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
}
