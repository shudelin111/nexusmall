package com.nexusmall.order.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Order {

    private Long id;
    private String orderSn;
    private Long memberId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItem> items;
}
