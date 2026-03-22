package com.nexusmall.order.controller;

import com.nexusmall.order.entity.Order;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.CreateOrderRequest;
import com.nexusmall.order.vo.UpdateOrderStatusRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderSn}")
    public Order getOrder(@PathVariable String orderSn) {
        return orderService.getByOrderSn(orderSn);
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(value = "memberId", required = false) Long memberId) {
        return orderService.listOrders(memberId);
    }

    @PutMapping("/{orderSn}/status")
    public Order updateStatus(@PathVariable String orderSn, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderSn, request);
    }
}
