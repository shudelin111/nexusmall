package com.nexusmall.order.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItem {

    private Long skuId;
    private String skuName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
