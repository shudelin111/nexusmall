package com.nexusmall.product.infrastructure.persistence.dao;

import lombok.Data;

@Data
public class ProductStockDTO {
    private Long skuId;
    private Integer count;
}
