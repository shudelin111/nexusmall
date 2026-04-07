package com.nexusmall.search.interfaces.dto;

import com.nexusmall.search.shared.enums.SearchSortType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SearchRequestVO {

    @NotBlank(message = "keyword must not be blank")
    private String keyword;
    @Min(value = 1, message = "pageNo must be greater than 0")
    private Integer pageNo = 1;
    @Min(value = 1, message = "pageSize must be greater than 0")
    private Integer pageSize = 20;
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
