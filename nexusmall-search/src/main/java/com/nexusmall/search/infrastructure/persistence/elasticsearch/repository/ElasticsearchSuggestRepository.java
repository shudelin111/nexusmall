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
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Elasticsearch 搜索建议 Repository
 * <p>
 * 使用 Completion Suggester 实现高性能自动补全
 * </p>
 */
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
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<SuggestItem>();
        }
        
        schemaManager.ensureIndex();
        
        try {
            // 优先使用 Completion Suggester（性能最优）
            List<SuggestItem> completionResults = suggestWithCompletion(keyword, limit);
            if (!completionResults.isEmpty()) {
                return completionResults;
            }
            
            // 降级方案：使用 matchPhrasePrefixQuery
            return suggestWithFallback(keyword, limit);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute suggest request", ex);
        }
    }

    /**
     * 使用 Completion Suggester（推荐，性能最优）
     */
    private List<SuggestItem> suggestWithCompletion(String keyword, int limit) throws IOException {
        SearchRequest request = new SearchRequest(schemaManager.getIndexName());
        
        // 构建 Completion Suggestion
        CompletionSuggestionBuilder nameSuggest = SuggestBuilders.completionSuggestion("nameSuggest")
                .prefix(keyword)
                .size(limit);
        
        CompletionSuggestionBuilder brandSuggest = SuggestBuilders.completionSuggestion("brandSuggest")
                .prefix(keyword)
                .size(limit);
        
        CompletionSuggestionBuilder categorySuggest = SuggestBuilders.completionSuggestion("categorySuggest")
                .prefix(keyword)
                .size(limit);
        
        request.source(new SearchSourceBuilder()
                .suggest(new org.elasticsearch.search.suggest.SuggestBuilder()
                        .addSuggestion("name_suggest", nameSuggest)
                        .addSuggestion("brand_suggest", brandSuggest)
                        .addSuggestion("category_suggest", categorySuggest)));
        
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return extractCompletionSuggestions(response, limit);
    }

    /**
     * 降级方案：使用 matchPhrasePrefixQuery
     */
    private List<SuggestItem> suggestWithFallback(String keyword, int limit) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhrasePrefixQuery("name", keyword).boost(3.0f))
                        .should(QueryBuilders.matchPhrasePrefixQuery("brandName", keyword).boost(2.0f))
                        .should(QueryBuilders.matchPhrasePrefixQuery("categoryName", keyword).boost(1.5f))
                        .minimumShouldMatch(1))
                .size(limit * 3);
        
        SearchRequest request = new SearchRequest(schemaManager.getIndexName());
        request.source(sourceBuilder);
        
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return extractSuggestions(response, limit);
    }

    /**
     * 提取 Completion Suggestion 结果
     */
    private List<SuggestItem> extractCompletionSuggestions(SearchResponse response, int limit) {
        Set<String> values = new LinkedHashSet<String>();
        List<SuggestItem> items = new ArrayList<SuggestItem>();
        
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return items;
        }
        
        // 提取商品名称建议
        extractSuggestionOption(suggest, "name_suggest", values, items, "product", limit);
        // 提取品牌建议
        extractSuggestionOption(suggest, "brand_suggest", values, items, "brand", limit);
        // 提取分类建议
        extractSuggestionOption(suggest, "category_suggest", values, items, "category", limit);
        
        return items;
    }

    private void extractSuggestionOption(Suggest suggest, String suggestionName, 
                                         Set<String> values, List<SuggestItem> items, 
                                         String type, int limit) {
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion = 
                suggest.getSuggestion(suggestionName);
        
        if (suggestion == null) {
            return;
        }
        
        for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : suggestion.getEntries()) {
            for (Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                if (items.size() >= limit) {
                    return;
                }
                
                String text = option.getText().string();
                if (values.add(type + ":" + text)) {
                    // Completion Suggester 不返回 score，使用固定值
                    items.add(new SuggestItem(text, type, 1.0, 1L));
                }
            }
        }
    }

    private List<SuggestItem> extractSuggestions(SearchResponse response, int limit) {
        Set<String> values = new LinkedHashSet<String>();
        List<SuggestItem> items = new ArrayList<SuggestItem>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            Long productCount = getProductCount(sourceMap);
            
            addIfAbsent(values, items, sourceMap.get("name"), "product", hit.getScore(), productCount, limit);
            addIfAbsent(values, items, sourceMap.get("brandName"), "brand", hit.getScore(), productCount, limit);
            addIfAbsent(values, items, sourceMap.get("categoryName"), "category", hit.getScore(), productCount, limit);
            if (items.size() >= limit) {
                break;
            }
        }
        return items;
    }

    private Long getProductCount(Map<String, Object> sourceMap) {
        // TODO: 使用 ES Terms Aggregation 统计每个关键词的商品数量
        return 1L;
    }

    private void addIfAbsent(Set<String> values, List<SuggestItem> items, Object rawValue, String type, float score, Long count, int limit) {
        if (rawValue == null || items.size() >= limit) {
            return;
        }
        String value = String.valueOf(rawValue);
        if (values.add(type + ":" + value)) {
            items.add(new SuggestItem(value, type, Double.valueOf(score), count));
        }
    }
}
