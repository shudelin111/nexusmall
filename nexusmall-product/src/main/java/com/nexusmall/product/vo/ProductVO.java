package com.nexusmall.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductVO implements Serializable {
    private static final long serialVersionUID = 1L;

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
}