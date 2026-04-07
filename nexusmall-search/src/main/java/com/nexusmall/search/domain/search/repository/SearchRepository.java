package com.nexusmall.search.domain.search.repository;

import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;

public interface SearchRepository {

    SearchResult search(SearchRequest request);
}
