package com.nexusmall.search.infrastructure.persistence.elasticsearch.repository;

import com.nexusmall.search.domain.search.model.Facet;
import com.nexusmall.search.domain.search.model.SearchItem;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.search.repository.SearchRepository;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.support.ProductIndexSchemaManager;
import com.nexusmall.search.shared.constant.SearchConstants;
import com.nexusmall.search.shared.enums.SearchSortType;
import com.nexusmall.search.shared.pagination.PageResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ElasticsearchSearchRepository implements SearchRepository {

    private final RestHighLevelClient client;
    private final ProductIndexSchemaManager schemaManager;

    public ElasticsearchSearchRepository(RestHighLevelClient client, ProductIndexSchemaManager schemaManager) {
        this.client = client;
        this.schemaManager = schemaManager;
    }

    @Override
    public SearchResult search(SearchRequest request) {
        schemaManager.ensureIndex();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(buildQuery(request));
        sourceBuilder.from((request.getPageNo() - 1) * request.getPageSize());
        sourceBuilder.size(request.getPageSize());
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("name")
                .field("subTitle")
                .preTags("<em>")
                .postTags("</em>"));
        addSort(request, sourceBuilder);
        addAggregations(sourceBuilder);

        org.elasticsearch.action.search.SearchRequest searchRequest =
                new org.elasticsearch.action.search.SearchRequest(schemaManager.getIndexName());
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return toSearchResult(response, request);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute search request", ex);
        }
    }

    private BoolQueryBuilder buildQuery(SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        MultiMatchQueryBuilder keywordQuery = QueryBuilders.multiMatchQuery(request.getKeyword(),
                        "name^5", "subTitle^3", "brandName^2", "categoryName^2", "searchableAttributes^2")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS);
        boolQuery.must(keywordQuery);
        boolQuery.filter(QueryBuilders.termQuery("status", 1));
        if (request.getCategoryId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", request.getCategoryId()));
        }
        if (request.getBrandId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("brandId", request.getBrandId()));
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            org.elasticsearch.index.query.RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (request.getMinPrice() != null) {
                rangeQuery.gte(request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                rangeQuery.lte(request.getMaxPrice());
            }
            boolQuery.filter(rangeQuery);
        }
        return boolQuery;
    }

    private void addSort(SearchRequest request, SearchSourceBuilder sourceBuilder) {
        if (request.getSortType() == null || request.getSortType() == SearchSortType.RELEVANCE) {
            sourceBuilder.sort("_score", SortOrder.DESC);
            return;
        }
        if (request.getSortType() == SearchSortType.PRICE_ASC) {
            sourceBuilder.sort("price", SortOrder.ASC);
            return;
        }
        if (request.getSortType() == SearchSortType.PRICE_DESC) {
            sourceBuilder.sort("price", SortOrder.DESC);
            return;
        }
        if (request.getSortType() == SearchSortType.SALES_DESC) {
            sourceBuilder.sort("stock", SortOrder.DESC);
            return;
        }
        sourceBuilder.sort("productId", SortOrder.DESC);
    }

    private void addAggregations(SearchSourceBuilder sourceBuilder) {
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms("categoryAgg")
                .field("categoryName.keyword")
                .size(20);
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg")
                .field("brandName.keyword")
                .size(20);
        sourceBuilder.aggregation(categoryAgg);
        sourceBuilder.aggregation(brandAgg);
    }

    private SearchResult toSearchResult(SearchResponse response, SearchRequest request) {
        SearchResult result = new SearchResult();
        List<SearchItem> items = new ArrayList<SearchItem>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            SearchItem item = new SearchItem();
            item.setProductId(asLong(source.get("productId")));
            item.setName(asString(source.get("name")));
            item.setSubTitle(asString(source.get("subTitle")));
            item.setCategoryName(asString(source.get("categoryName")));
            item.setBrandName(asString(source.get("brandName")));
            item.setPrice(asBigDecimal(source.get("price")));
            item.setCurrency(SearchConstants.DEFAULT_CURRENCY);
            item.setMainImage(asString(source.get("mainImage")));
            item.setHighlightSnippets(extractHighlights(hit));
            items.add(item);
        }
        result.setPage(new PageResponse<SearchItem>(items, response.getHits().getTotalHits().value,
                request.getPageNo(), request.getPageSize()));
        result.setFacets(extractFacets(response));
        return result;
    }

    private List<String> extractHighlights(SearchHit hit) {
        List<String> highlights = new ArrayList<String>();
        for (Map.Entry<String, HighlightField> entry : hit.getHighlightFields().entrySet()) {
            if (entry.getValue() != null && entry.getValue().fragments() != null) {
                for (int i = 0; i < entry.getValue().fragments().length; i++) {
                    highlights.add(entry.getValue().fragments()[i].string());
                }
            }
        }
        return highlights;
    }

    private List<Facet> extractFacets(SearchResponse response) {
        List<Facet> facets = new ArrayList<Facet>();
        facets.add(buildFacet("category", response.getAggregations().get("categoryAgg")));
        facets.add(buildFacet("brand", response.getAggregations().get("brandAgg")));
        return facets;
    }

    private Facet buildFacet(String name, Terms terms) {
        Facet facet = new Facet();
        facet.setName(name);
        List<Facet.FacetValue> values = new ArrayList<Facet.FacetValue>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                values.add(new Facet.FacetValue(bucket.getKeyAsString(), bucket.getDocCount()));
            }
        }
        facet.setValues(values);
        return facet;
    }

    private Long asLong(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal asBigDecimal(Object value) {
        return value == null ? null : new BigDecimal(String.valueOf(value));
    }
}
