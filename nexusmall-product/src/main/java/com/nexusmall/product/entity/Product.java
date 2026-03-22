package com.nexusmall.product.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Product {

    private Long skuId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private String categoryName;
    private String brandName;
    private String description;
}
