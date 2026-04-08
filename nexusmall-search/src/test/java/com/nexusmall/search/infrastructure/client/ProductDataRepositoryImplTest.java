package com.nexusmall.search.infrastructure.client;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.infrastructure.client.dto.ProductClientDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class ProductDataRepositoryImplTest {

    @Test
    void shouldConvertSingleProductToIndexDocument() {
        ProductClient productClient = Mockito.mock(ProductClient.class);
        ProductClientDTO dto = product(1001L, "Keyboard", "Keyboards", "Nexus", 1);
        Mockito.when(productClient.getProduct(Mockito.eq("v1"), Mockito.eq(1001L)))
                .thenReturn(Result.success(dto));

        ProductDataRepositoryImpl repository = new ProductDataRepositoryImpl(productClient);
        ProductIndexDocument document = repository.findByProductId(1001L);

        Assertions.assertNotNull(document);
        Assertions.assertEquals("Keyboard", document.getName());
        Assertions.assertEquals("Keyboards", document.getCategoryName());
        Assertions.assertEquals(2, document.getSearchableAttributes().size());
    }

    @Test
    void shouldRequestOnSaleProductsWhenListingIndexSource() {
        ProductClient productClient = Mockito.mock(ProductClient.class);
        Mockito.when(productClient.searchProducts(Mockito.eq("v1"), Mockito.any()))
                .thenReturn(Result.success(Collections.singletonList(product(1002L, "Mouse", "Accessories", "Nexus", 1))));

        ProductDataRepositoryImpl repository = new ProductDataRepositoryImpl(productClient);
        List<ProductIndexDocument> documents = repository.listOnSaleProducts();

        Assertions.assertEquals(1, documents.size());
        Assertions.assertEquals("Mouse", documents.get(0).getName());

        ArgumentCaptor<com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest> captor =
                ArgumentCaptor.forClass(com.nexusmall.search.infrastructure.client.dto.ProductQueryClientRequest.class);
        Mockito.verify(productClient).searchProducts(Mockito.eq("v1"), captor.capture());
        Assertions.assertEquals(Integer.valueOf(1), captor.getValue().getStatus());
    }

    @Test
    void shouldReturnNullWhenClientFails() {
        ProductClient productClient = Mockito.mock(ProductClient.class);
        Mockito.when(productClient.getProduct(Mockito.eq("v1"), Mockito.eq(999L)))
                .thenReturn(Result.failure("ERR", "failed"));

        ProductDataRepositoryImpl repository = new ProductDataRepositoryImpl(productClient);

        Assertions.assertNull(repository.findByProductId(999L));
    }

    private ProductClientDTO product(Long skuId, String skuName, String categoryName, String brandName, Integer status) {
        ProductClientDTO dto = new ProductClientDTO();
        dto.setSkuId(skuId);
        dto.setSkuName(skuName);
        dto.setDescription(skuName + " description");
        dto.setCategoryId(1L);
        dto.setCategoryName(categoryName);
        dto.setBrandId(2L);
        dto.setBrandName(brandName);
        dto.setPrice(new BigDecimal("99.00"));
        dto.setStock(20);
        dto.setStatus(status);
        return dto;
    }
}
