package com.nexusmall.order.controller;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.OrderCreateRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 健康检查接口
     */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "nexusmall-order");
        payload.put("status", "UP");
        payload.put("message", "order service is ready");
        return Result.success(payload);
    }

    /**
     * 根据 ID 查询订单
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable("id") Long id) {
        Order order = orderService.getById(id);
        return order != null ? Result.success(order) : Result.failure(CommonResultCode.NOT_FOUND.getCode(), "订单不存在");
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/sn/{orderSn}")
    public Result<Order> getOrderByOrderSn(@PathVariable("orderSn") String orderSn) {
        Order order = orderService.getByOrderSn(orderSn);
        return order != null ? Result.success(order) : Result.failure(CommonResultCode.NOT_FOUND.getCode(), "订单不存在");
    }

    /**
     * 查询所有订单
     */
    @GetMapping("/list")
    public Result<List<Order>> listOrders() {
        List<Order> orders = orderService.list();
        return Result.success(orders);
    }

    /**
     * 根据用户 ID 查询订单列表
     */
    @GetMapping("/member/{memberId}")
    public Result<List<Order>> listByMemberId(@PathVariable("memberId") Long memberId) {
        List<Order> orders = orderService.listByMemberId(memberId);
        return Result.success(orders);
    }

    /**
     * 根据订单状态查询订单列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Order>> listByStatus(@PathVariable("status") Integer status) {
        List<Order> orders = orderService.listByStatus(status);
        return Result.success(orders);
    }

    /**
     * 条件查询订单列表
     */
    @GetMapping("/search")
    public Result<List<Order>> searchOrders(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Order> orders = orderService.listByCondition(memberId, status, startTime, endTime);
        return Result.success(orders);
    }

    /**
     * 创建订单（带分布式事务）
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(request);
        return Result.success("订单创建成功", order);
    }

    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateOrder(@PathVariable("id") Long id, @RequestBody Order order) {
        order.setId(id);
        boolean result = orderService.updateById(order);
        return result ? Result.success("订单更新成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单更新失败");
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteOrder(@PathVariable("id") Long id) {
        boolean result = orderService.deleteById(id);
        return result ? Result.success("订单删除成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单删除失败");
    }

    /**
     * 批量删除订单
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteOrders(@RequestBody List<Long> ids) {
        boolean result = orderService.batchDelete(ids);
        return result ? Result.success("批量删除成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "批量删除失败");
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<Boolean> payOrder(
            @PathVariable("id") Long id,
            @RequestParam("paymentType") Integer paymentType) {
        boolean result = orderService.payOrder(id, paymentType);
        return result ? Result.success("支付成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "支付失败");
    }

    /**
     * 发货
     */
    @PostMapping("/{id}/deliver")
    public Result<Boolean> deliverOrder(@PathVariable("id") Long id) {
        boolean result = orderService.deliveryOrder(id);
        return result ? Result.success("发货成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "发货失败");
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/receive")
    public Result<Boolean> receiveOrder(@PathVariable("id") Long id) {
        boolean result = orderService.receiveOrder(id);
        return result ? Result.success("确认收货成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "确认收货失败");
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable("id") Long id) {
        boolean result = orderService.cancelOrder(id);
        return result ? Result.success("订单取消成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单取消失败");
    }
}
