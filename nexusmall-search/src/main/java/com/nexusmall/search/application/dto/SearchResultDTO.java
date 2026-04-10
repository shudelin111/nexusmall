package com.nexusmall.search.application.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResultDTO {

    private List<SearchItemDTO> items = new ArrayList<SearchItemDTO>();
    private List<AggregationDTO> aggregations = new ArrayList<AggregationDTO>();
    private List<String> correctedKeywords = new ArrayList<String>();
    private Long total;
    private Integer pageNo;
    private Integer pageSize;

    public List<SearchItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SearchItemDTO> items) {
        this.items = items;
    }

    public List<AggregationDTO> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<AggregationDTO> aggregations) {
        this.aggregations = aggregations;
    }

    public List<String> getCorrectedKeywords() {
        return correctedKeywords;
    }

    public void setCorrectedKeywords(List<String> correctedKeywords) {
        this.correctedKeywords = correctedKeywords;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
