package com.nexusmall.search.interfaces.rest;

import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SuggestControllerTest {

    private SearchFacade searchFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        searchFacade = Mockito.mock(SearchFacade.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SuggestController(searchFacade)).build();
    }

    @Test
    void shouldReturnSuggestResult() throws Exception {
        SuggestItemDTO item = new SuggestItemDTO();
        item.setKeyword("keyboard");
        item.setType("product");
        item.setScore(1.0D);

        Mockito.when(searchFacade.suggest(Mockito.any())).thenReturn(Collections.singletonList(item));

        mockMvc.perform(get("/search/suggest")
                        .header("X-API-Version", "v1")
                        .param("keyword", "key")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].keyword").value("keyboard"))
                .andExpect(jsonPath("$.data.items[0].type").value("product"));
    }
}
