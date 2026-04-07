package com.nexusmall.search.interfaces.rest;

import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IndexControllerTest {

    private SearchFacade searchFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        searchFacade = Mockito.mock(SearchFacade.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new IndexController(searchFacade)).build();
    }

    @Test
    void shouldBuildSingleIndex() throws Exception {
        mockMvc.perform(post("/search/index/products")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1001,\"name\":\"Keyboard\",\"brandName\":\"Nexus\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Mockito.verify(searchFacade).buildIndex(Mockito.argThat(command ->
                command != null && Long.valueOf(1001L).equals(command.getProductId()) && "Keyboard".equals(command.getName())));
    }

    @Test
    void shouldRebuildIndex() throws Exception {
        Mockito.when(searchFacade.rebuildIndex(Mockito.any())).thenReturn(2);

        mockMvc.perform(post("/search/index/rebuild")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullRebuild\":true,\"products\":[{\"productId\":1001,\"name\":\"Keyboard\"},{\"productId\":1002,\"name\":\"Mouse\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));

        ArgumentCaptor<com.nexusmall.search.application.command.RebuildIndexCommand> captor =
                ArgumentCaptor.forClass(com.nexusmall.search.application.command.RebuildIndexCommand.class);
        Mockito.verify(searchFacade).rebuildIndex(captor.capture());
        assertEquals(2, captor.getValue().getProducts().size());
        assertEquals(true, captor.getValue().isFullRebuild());
    }

    @Test
    void shouldDeleteIndex() throws Exception {
        mockMvc.perform(delete("/search/index/products/1003")
                        .header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Mockito.verify(searchFacade).removeIndex(Mockito.argThat(command ->
                command != null && Long.valueOf(1003L).equals(command.getProductId())));
    }
}
