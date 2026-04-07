package com.nexusmall.order.interfaces.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "订单状态不能为空")
    private String status;
}
