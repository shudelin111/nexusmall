package com.nexusmall.search.infrastructure.persistence.elasticsearch.repository;

import com.nexusmall.search.domain.suggest.model.SuggestItem;
import com.nexusmall.search.domain.suggest.repository.SuggestRepository;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.support.ProductIndexSchemaManager;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class ElasticsearchSuggestRepository implements SuggestRepository {

    private final RestHighLevelClient client;
    private final ProductIndexSchemaManager schemaManager;

    public ElasticsearchSuggestRepository(RestHighLevelClient client, ProductIndexSchemaManager schemaManager) {
        this.client = client;
        this.schemaManager = schemaManager;
    }

    @Override
    public List<SuggestItem> suggest(String keyword, int limit) {
        schemaManager.ensureIndex();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhrasePrefixQuery("name", keyword))
                        .should(QueryBuilders.matchPhrasePrefixQuery("brandName", keyword))
                        .should(QueryBuilders.matchPhrasePrefixQuery("categoryName", keyword))
                        .minimumShouldMatch(1))
                .size(limit * 3);
        SearchRequest request = new SearchRequest(schemaManager.getIndexName());
        request.source(sourceBuilder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return extractSuggestions(response, limit);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute suggest request", ex);
        }
    }

    private List<SuggestItem> extractSuggestions(SearchResponse response, int limit) {
        Set<String> values = new LinkedHashSet<String>();
        List<SuggestItem> items = new ArrayList<SuggestItem>();
        for (SearchHit hit : response.getHits().getHits()) {
            addIfAbsent(values, items, hit.getSourceAsMap().get("name"), "product", hit.getScore(), limit);
            addIfAbsent(values, items, hit.getSourceAsMap().get("brandName"), "brand", hit.getScore(), limit);
            addIfAbsent(values, items, hit.getSourceAsMap().get("categoryName"), "category", hit.getScore(), limit);
            if (items.size() >= limit) {
                break;
            }
        }
        return items;
    }

    private void addIfAbsent(Set<String> values, List<SuggestItem> items, Object rawValue, String type, float score, int limit) {
        if (rawValue == null || items.size() >= limit) {
            return;
        }
        String value = String.valueOf(rawValue);
        if (values.add(type + ":" + value)) {
            items.add(new SuggestItem(value, type, Double.valueOf(score)));
        }
    }
}
