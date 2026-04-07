package com.nexusmall.search.application.assembler;

import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.dto.AggregationDTO;
import com.nexusmall.search.application.dto.SearchItemDTO;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.search.model.Facet;
import com.nexusmall.search.domain.search.model.SearchItem;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.suggest.model.SuggestItem;
import com.nexusmall.search.shared.constant.SearchConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchAssembler {

    public SearchRequest toSearchRequest(ProductSearchQuery query) {
        SearchRequest request = new SearchRequest();
        request.setKeyword(query.getKeyword());
        request.setPageNo(query.getPageNo());
        request.setPageSize(query.getPageSize());
        request.setSortType(query.getSortType());
        request.setCategoryId(query.getCategoryId());
        request.setBrandId(query.getBrandId());
        request.setMinPrice(query.getMinPrice());
        request.setMaxPrice(query.getMaxPrice());
        request.setAttributeFilters(query.getAttributeFilters());
        return request;
    }

    public SearchResultDTO toSearchResultDTO(SearchResult result) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setTotal(result.getPage().getTotal());
        dto.setPageNo(result.getPage().getPageNo());
        dto.setPageSize(result.getPage().getPageSize());
        dto.setCorrectedKeywords(result.getCorrectedKeywords());
        dto.setItems(toSearchItemDTOs(result.getPage().getRecords()));
        dto.setAggregations(toAggregationDTOs(result.getFacets()));
        return dto;
    }

    public List<SuggestItemDTO> toSuggestItemDTOs(List<SuggestItem> items) {
        List<SuggestItemDTO> result = new ArrayList<SuggestItemDTO>();
        for (SuggestItem item : items) {
            SuggestItemDTO dto = new SuggestItemDTO();
            dto.setKeyword(item.getKeyword());
            dto.setType(item.getType());
            dto.setScore(item.getScore());
            result.add(dto);
        }
        return result;
    }

    public ProductIndexDocument toIndexDocument(BuildProductIndexCommand command) {
        ProductIndexDocument document = new ProductIndexDocument();
        document.setProductId(command.getProductId());
        document.setName(command.getName());
        document.setSubTitle(command.getSubTitle());
        document.setCategoryId(command.getCategoryId());
        document.setCategoryName(command.getCategoryName());
        document.setBrandId(command.getBrandId());
        document.setBrandName(command.getBrandName());
        document.setPrice(command.getPrice());
        document.setMainImage(command.getMainImage());
        document.setStock(command.getStock());
        document.setStatus(command.getStatus());
        document.setSearchableAttributes(command.getSearchableAttributes());
        return document;
    }

    private List<SearchItemDTO> toSearchItemDTOs(List<SearchItem> items) {
        List<SearchItemDTO> result = new ArrayList<SearchItemDTO>();
        for (SearchItem item : items) {
            SearchItemDTO dto = new SearchItemDTO();
            dto.setProductId(item.getProductId());
            dto.setName(item.getName());
            dto.setSubTitle(item.getSubTitle());
            dto.setCategoryName(item.getCategoryName());
            dto.setBrandName(item.getBrandName());
            dto.setPrice(item.getPrice());
            dto.setCurrency(item.getCurrency() == null ? SearchConstants.DEFAULT_CURRENCY : item.getCurrency());
            dto.setMainImage(item.getMainImage());
            dto.setHighlights(item.getHighlightSnippets());
            result.add(dto);
        }
        return result;
    }

    private List<AggregationDTO> toAggregationDTOs(List<Facet> facets) {
        List<AggregationDTO> result = new ArrayList<AggregationDTO>();
        for (Facet facet : facets) {
            AggregationDTO dto = new AggregationDTO();
            dto.setName(facet.getName());
            List<AggregationDTO.BucketDTO> buckets = new ArrayList<AggregationDTO.BucketDTO>();
            for (Facet.FacetValue value : facet.getValues()) {
                AggregationDTO.BucketDTO bucket = new AggregationDTO.BucketDTO();
                bucket.setLabel(value.getLabel());
                bucket.setCount(value.getCount());
                buckets.add(bucket);
            }
            dto.setBuckets(buckets);
            result.add(dto);
        }
        return result;
    }
}
