package com.nexusmall.order.vo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "联系电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    @Valid
    @NotEmpty(message = "订单项不能为空")
    private List<CreateOrderItemRequest> items;
}
