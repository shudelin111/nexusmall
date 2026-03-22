package com.nexusmall.order.vo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {

    @NotNull(message = "商品SKU不能为空")
    private Long skuId;

    @NotBlank(message = "商品名称不能为空")
    private String skuName;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

    @NotNull(message = "商品单价不能为空")
    @DecimalMin(value = "0.01", message = "商品单价必须大于0")
    private BigDecimal price;
}
