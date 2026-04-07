package com.nexusmall.search.application.service;

import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.search.service.SearchDomainService;
import org.springframework.stereotype.Service;

@Service
public class SearchApplicationService {

    private final SearchDomainService searchDomainService;
    private final SearchAssembler searchAssembler;

    public SearchApplicationService(SearchDomainService searchDomainService, SearchAssembler searchAssembler) {
        this.searchDomainService = searchDomainService;
        this.searchAssembler = searchAssembler;
    }

    public SearchResultDTO search(ProductSearchQuery query) {
        SearchResult result = searchDomainService.search(searchAssembler.toSearchRequest(query));
        return searchAssembler.toSearchResultDTO(result);
    }
}
