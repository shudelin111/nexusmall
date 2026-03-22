package com.nexusmall.order.service;

import com.nexusmall.order.entity.Order;
import com.nexusmall.order.vo.CreateOrderRequest;
import com.nexusmall.order.vo.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    Order getByOrderSn(String orderSn);

    List<Order> listOrders(Long memberId);

    Order updateStatus(String orderSn, UpdateOrderStatusRequest request);
}
