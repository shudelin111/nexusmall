package com.nexusmall.product.application.service.impl;

import com.nexusmall.product.domain.entity.Product;
import com.nexusmall.product.infrastructure.persistence.dao.ProductMapper;
import com.nexusmall.product.infrastructure.persistence.dao.ProductStockDTO;
import com.nexusmall.product.interfaces.dto.ProductQueryRequest;
import com.nexusmall.product.interfaces.dto.ProductVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ProductServiceImplTest {

    @Test
    void shouldSetSkuIdBackToVoWhenSaving() {
        ProductMapper mapper = Mockito.mock(ProductMapper.class);
        Mockito.when(mapper.insert(Mockito.any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setSkuId(1001L);
            return 1;
        });

        ProductServiceImpl service = new ProductServiceImpl(mapper);
        ProductVO vo = new ProductVO();
        vo.setSkuName("Keyboard");

        int rows = service.save(vo);

        Assertions.assertEquals(1, rows);
        Assertions.assertEquals(1001L, vo.getSkuId());
    }

    @Test
    void shouldConvertConditionQueryResultsToVoList() {
        ProductMapper mapper = Mockito.mock(ProductMapper.class);
        Product product = new Product();
        product.setSkuId(1002L);
        product.setSkuName("Mouse");
        Mockito.when(mapper.listByCondition("mouse", 1L, 2L, 1))
                .thenReturn(Collections.singletonList(product));

        ProductServiceImpl service = new ProductServiceImpl(mapper);
        ProductQueryRequest request = new ProductQueryRequest();
        request.setKeyword("mouse");
        request.setCategoryId(1L);
        request.setBrandId(2L);
        request.setStatus(1);

        List<ProductVO> result = service.listByCondition(request);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Mouse", result.get(0).getSkuName());
    }

    @Test
    void shouldReturnFalseWhenBatchDecreaseContainsFailure() {
        ProductMapper mapper = Mockito.mock(ProductMapper.class);
        Mockito.when(mapper.decreaseStock(1001L, 1)).thenReturn(1);
        Mockito.when(mapper.decreaseStock(1002L, 1)).thenReturn(0);

        ProductServiceImpl service = new ProductServiceImpl(mapper);
        ProductStockDTO first = new ProductStockDTO();
        first.setSkuId(1001L);
        first.setCount(1);
        ProductStockDTO second = new ProductStockDTO();
        second.setSkuId(1002L);
        second.setCount(1);

        boolean result = service.batchDecreaseStock(Arrays.asList(first, second));

        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenPutOnSaleUpdatesRows() {
        ProductMapper mapper = Mockito.mock(ProductMapper.class);
        Mockito.when(mapper.putOnSale(1003L)).thenReturn(1);

        ProductServiceImpl service = new ProductServiceImpl(mapper);

        Assertions.assertTrue(service.putOnSale(1003L));
    }
}
