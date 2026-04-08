package com.nexusmall.search.application.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResultDTO {

    private List<SearchItemDTO> items = new ArrayList<SearchItemDTO>();
    private List<AggregationDTO> aggregations = new ArrayList<AggregationDTO>();
    private List<String> correctedKeywords = new ArrayList<String>();
    private long total;
    private int pageNo;
    private int pageSize;

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

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
