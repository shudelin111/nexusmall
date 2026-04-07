package com.nexusmall.search.application.assembler;

import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.domain.search.model.SearchItem;
import com.nexusmall.search.domain.search.model.SearchResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

class SearchAssemblerTest {

    private final SearchAssembler searchAssembler = new SearchAssembler();

    @Test
    void shouldMapDomainResultToDto() {
        SearchItem item = new SearchItem();
        item.setProductId(1001L);
        item.setName("Mechanical Keyboard");
        item.setPrice(new BigDecimal("399.00"));
        item.setHighlightSnippets(Collections.singletonList("<em>Mechanical</em> Keyboard"));

        SearchResult result = new SearchResult();
        result.getPage().setRecords(Collections.singletonList(item));
        result.getPage().setTotal(1L);
        result.getPage().setPageNo(2);
        result.getPage().setPageSize(10);

        SearchResultDTO dto = searchAssembler.toSearchResultDTO(result);

        Assertions.assertEquals(1L, dto.getTotal());
        Assertions.assertEquals(2, dto.getPageNo());
        Assertions.assertEquals(1, dto.getItems().size());
        Assertions.assertEquals("CNY", dto.getItems().get(0).getCurrency());
    }
}
