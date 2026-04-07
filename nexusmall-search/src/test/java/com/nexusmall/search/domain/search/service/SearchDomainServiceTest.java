package com.nexusmall.search.domain.search.service;

import com.nexusmall.search.config.SearchProperties;
import com.nexusmall.search.domain.search.model.SearchRequest;
import com.nexusmall.search.domain.search.model.SearchResult;
import com.nexusmall.search.domain.search.repository.SearchRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchDomainServiceTest {

    @Test
    void shouldNormalizePageArgumentsBeforeSearch() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultPageNo(1);
        properties.setDefaultPageSize(20);
        properties.setMaxPageSize(50);

        SearchRepository repository = new SearchRepository() {
            @Override
            public SearchResult search(SearchRequest request) {
                SearchResult result = new SearchResult();
                result.getPage().setPageNo(request.getPageNo());
                result.getPage().setPageSize(request.getPageSize());
                return result;
            }
        };

        SearchDomainService service = new SearchDomainService(repository, properties);
        SearchRequest request = new SearchRequest();
        request.setKeyword("phone");
        request.setPageNo(0);
        request.setPageSize(999);

        SearchResult result = service.search(request);

        Assertions.assertEquals(1, result.getPage().getPageNo());
        Assertions.assertEquals(50, result.getPage().getPageSize());
    }

    @Test
    void shouldRejectBlankKeyword() {
        SearchProperties properties = new SearchProperties();
        SearchDomainService service = new SearchDomainService(new SearchRepository() {
            @Override
            public SearchResult search(SearchRequest request) {
                return new SearchResult();
            }
        }, properties);

        SearchRequest request = new SearchRequest();
        request.setKeyword(" ");

        Assertions.assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                service.search(request);
            }
        });
    }
}
