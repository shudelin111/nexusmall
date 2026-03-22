package com.nexusmall.order.controller;

import com.nexusmall.common.vo.Result;
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
    public Result<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    @GetMapping("/{orderSn}")
    public Result<Order> getOrder(@PathVariable String orderSn) {
        return Result.success(orderService.getByOrderSn(orderSn));
    }

    @GetMapping
    public Result<List<Order>> listOrders(@RequestParam(value = "memberId", required = false) Long memberId) {
        return Result.success(orderService.listOrders(memberId));
    }

    @PutMapping("/{orderSn}/status")
    public Result<Order> updateStatus(@PathVariable String orderSn, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return Result.success(orderService.updateStatus(orderSn, request));
    }
}
