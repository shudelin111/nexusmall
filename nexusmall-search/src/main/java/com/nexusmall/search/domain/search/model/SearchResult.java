package com.nexusmall.search.domain.search.model;

import com.nexusmall.search.shared.pagination.PageResponse;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    private PageResponse<SearchItem> page = new PageResponse<SearchItem>();
    private List<Facet> facets = new ArrayList<Facet>();
    private List<String> correctedKeywords = new ArrayList<String>();

    public PageResponse<SearchItem> getPage() {
        return page;
    }

    public void setPage(PageResponse<SearchItem> page) {
        this.page = page;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    public List<String> getCorrectedKeywords() {
        return correctedKeywords;
    }

    public void setCorrectedKeywords(List<String> correctedKeywords) {
        this.correctedKeywords = correctedKeywords;
    }
}
