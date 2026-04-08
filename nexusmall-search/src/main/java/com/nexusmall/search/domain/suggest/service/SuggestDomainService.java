package com.nexusmall.search.domain.suggest.service;

import com.nexusmall.search.domain.suggest.model.SuggestItem;
import com.nexusmall.search.domain.suggest.repository.SuggestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestDomainService {

    private final SuggestRepository suggestRepository;

    public SuggestDomainService(SuggestRepository suggestRepository) {
        this.suggestRepository = suggestRepository;
    }

    public List<SuggestItem> suggest(String keyword, int limit) {
        return suggestRepository.suggest(keyword, limit);
    }
}
