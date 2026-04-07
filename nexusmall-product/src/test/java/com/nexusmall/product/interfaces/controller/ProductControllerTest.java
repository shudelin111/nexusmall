package com.nexusmall.product.interfaces.controller;

import com.nexusmall.common.message.ProductIndexSyncEvent;
import com.nexusmall.common.message.ProductIndexSyncEventType;
import com.nexusmall.product.application.service.ProductService;
import com.nexusmall.product.interfaces.dto.ProductVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private ProductService productService;
    private RocketMQTemplate rocketMQTemplate;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        rocketMQTemplate = Mockito.mock(RocketMQTemplate.class);
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", productService);
        ReflectionTestUtils.setField(controller, "rocketMQTemplate", rocketMQTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldPublishUpsertEventOnSave() throws Exception {
        Mockito.when(productService.save(Mockito.any(ProductVO.class))).thenAnswer(invocation -> {
            ProductVO productVO = invocation.getArgument(0);
            productVO.setSkuId(1001L);
            return 1;
        });

        mockMvc.perform(post("/products/")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuName\":\"Keyboard\",\"categoryId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verifyEvent(1001L, ProductIndexSyncEventType.UPSERT);
    }

    @Test
    void shouldPublishRemoveEventOnDelete() throws Exception {
        Mockito.when(productService.deleteById(1002L)).thenReturn(1);

        mockMvc.perform(delete("/products/1002")
                        .header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verifyEvent(1002L, ProductIndexSyncEventType.REMOVE);
    }

    @Test
    void shouldPublishUpsertEventOnPutOnSale() throws Exception {
        Mockito.when(productService.putOnSale(1003L)).thenReturn(true);

        mockMvc.perform(patch("/products/1003/on-sale")
                        .header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verifyEvent(1003L, ProductIndexSyncEventType.UPSERT);
    }

    @Test
    void shouldPublishUpsertEventOnUpdate() throws Exception {
        Mockito.when(productService.updateById(Mockito.any(ProductVO.class))).thenReturn(1);

        mockMvc.perform(put("/products/1004")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuName\":\"Updated Keyboard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verifyEvent(1004L, ProductIndexSyncEventType.UPSERT);
    }

    private void verifyEvent(Long productId, ProductIndexSyncEventType eventType) {
        ArgumentCaptor<ProductIndexSyncEvent> captor = ArgumentCaptor.forClass(ProductIndexSyncEvent.class);
        Mockito.verify(rocketMQTemplate).convertAndSend(Mockito.eq("PRODUCT_INDEX_SYNC_TOPIC"), captor.capture());
        ProductIndexSyncEvent event = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(productId, event.getProductId());
        org.junit.jupiter.api.Assertions.assertEquals(eventType, event.getEventType());
    }
}
