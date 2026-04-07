package com.nexusmall.search.domain.search.model;

import com.nexusmall.search.shared.enums.SearchSortType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SearchRequest {

    private String keyword;
    private Integer pageNo;
    private Integer pageSize;
    private SearchSortType sortType = SearchSortType.RELEVANCE;
    private Long categoryId;
    private Long brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> attributeFilters = new ArrayList<String>();

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public SearchSortType getSortType() {
        return sortType;
    }

    public void setSortType(SearchSortType sortType) {
        this.sortType = sortType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<String> getAttributeFilters() {
        return attributeFilters;
    }

    public void setAttributeFilters(List<String> attributeFilters) {
        this.attributeFilters = attributeFilters;
    }
}
