package com.nexusmall.product.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    private Long skuId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version;
}
