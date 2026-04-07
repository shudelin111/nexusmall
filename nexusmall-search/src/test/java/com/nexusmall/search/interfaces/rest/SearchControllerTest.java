package com.nexusmall.search.interfaces.rest;

import com.nexusmall.search.application.dto.SearchItemDTO;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest {

    private SearchFacade searchFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        searchFacade = Mockito.mock(SearchFacade.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SearchController(searchFacade)).build();
    }

    @Test
    void shouldReturnSearchResult() throws Exception {
        SearchItemDTO item = new SearchItemDTO();
        item.setProductId(1001L);
        item.setName("Mechanical Keyboard");
        item.setPrice(new BigDecimal("399.00"));

        SearchResultDTO result = new SearchResultDTO();
        result.setItems(Collections.singletonList(item));
        result.setTotal(1L);
        result.setPageNo(1);
        result.setPageSize(20);

        Mockito.when(searchFacade.search(Mockito.any())).thenReturn(result);

        mockMvc.perform(get("/search/products")
                        .header("X-API-Version", "v1")
                        .param("keyword", "keyboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Mechanical Keyboard"));
    }
}
