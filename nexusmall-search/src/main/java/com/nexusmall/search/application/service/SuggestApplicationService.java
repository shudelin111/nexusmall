package com.nexusmall.search.application.service;

import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.SuggestQuery;
import com.nexusmall.search.domain.suggest.service.SuggestDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestApplicationService {

    private final SuggestDomainService suggestDomainService;
    private final SearchAssembler searchAssembler;

    public SuggestApplicationService(SuggestDomainService suggestDomainService, SearchAssembler searchAssembler) {
        this.suggestDomainService = suggestDomainService;
        this.searchAssembler = searchAssembler;
    }

    public List<SuggestItemDTO> suggest(SuggestQuery query) {
        int limit = query.getLimit() == null || query.getLimit() < 1 ? 10 : query.getLimit();
        return searchAssembler.toSuggestItemDTOs(suggestDomainService.suggest(query.getKeyword(), limit));
    }
}
