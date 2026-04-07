package com.nexusmall.search.domain.suggest.repository;

import com.nexusmall.search.domain.suggest.model.SuggestItem;

import java.util.List;

public interface SuggestRepository {

    List<SuggestItem> suggest(String keyword, int limit);
}
