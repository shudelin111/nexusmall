package com.nexusmall.search.domain.search.service;

import com.nexusmall.search.config.SearchProperties;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.search.repository.SearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SearchDomainService {

    private final SearchRepository searchRepository;
    private final SearchProperties searchProperties;

    public SearchDomainService(SearchRepository searchRepository, SearchProperties searchProperties) {
        this.searchRepository = searchRepository;
        this.searchProperties = searchProperties;
    }

    public SearchResult search(SearchRequest request) {
        normalize(request);
        return searchRepository.search(request);
    }

    private void normalize(SearchRequest request) {
        if (!StringUtils.hasText(request.getKeyword())) {
            throw new IllegalArgumentException("Search keyword must not be blank");
        }
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(searchProperties.getDefaultPageNo());
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(searchProperties.getDefaultPageSize());
        }
        if (request.getPageSize() > searchProperties.getMaxPageSize()) {
            request.setPageSize(searchProperties.getMaxPageSize());
        }
    }
}
