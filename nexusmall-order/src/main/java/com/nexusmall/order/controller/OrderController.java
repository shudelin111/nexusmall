package com.nexusmall.order.controller;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.OrderCreateRequest;
import com.nexusmall.order.vo.OrderQueryRequest;
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
        log.info("查询订单，orderId: {}", id);
        Order order = orderService.getById(id);
        if (order != null) {
            log.info("订单查询成功，orderId: {}, orderSn: {}", id, order.getOrderSn());
            return Result.success(order);
        } else {
            log.warn("订单不存在，orderId: {}", id);
            return Result.failure(CommonResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/sn/{orderSn}")
    public Result<Order> getOrderByOrderSn(@PathVariable("orderSn") String orderSn) {
        log.info("查询订单，orderSn: {}", orderSn);
        Order order = orderService.getByOrderSn(orderSn);
        if (order != null) {
            log.info("订单查询成功，orderSn: {}, orderId: {}", orderSn, order.getId());
            return Result.success(order);
        } else {
            log.warn("订单不存在，orderSn: {}", orderSn);
            return Result.failure(CommonResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
    }

    /**
     * 查询所有订单
     */
    @GetMapping("/list")
    public Result<List<Order>> listOrders() {
        log.info("查询所有订单");
        List<Order> orders = orderService.list();
        log.info("查询到{}条订单", orders.size());
        return Result.success(orders);
    }

    /**
     * 根据用户 ID 查询订单列表
     */
    @GetMapping("/member/{memberId}")
    public Result<List<Order>> listByMemberId(@PathVariable("memberId") Long memberId) {
        log.info("查询用户订单，memberId: {}", memberId);
        List<Order> orders = orderService.listByMemberId(memberId);
        log.info("用户 {} 查询到{}条订单", memberId, orders.size());
        return Result.success(orders);
    }

    /**
     * 根据订单状态查询订单列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Order>> listByStatus(@PathVariable("status") Integer status) {
        log.info("查询订单，status: {}", status);
        List<Order> orders = orderService.listByStatus(status);
        log.info("状态 {} 查询到{}条订单", status, orders.size());
        return Result.success(orders);
    }

    /**
     * 条件查询订单列表
     */
    @GetMapping("/search")
    public Result<List<Order>> searchOrders(@ModelAttribute OrderQueryRequest request) {
        log.info("条件查询订单，memberId: {}, status: {}, startTime: {}, endTime: {}", 
                request.getMemberId(), request.getStatus(), request.getStartTime(), request.getEndTime());
        List<Order> orders = orderService.listByCondition(request);
        log.info("条件查询到{}条订单", orders.size());
        return Result.success(orders);
    }

    /**
     * 创建订单（带分布式事务）
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        log.info("收到创建订单请求，userId: {}, productId: {}, count: {}", 
                request.getMemberId(), request.getProductId(), request.getCount());
        
        Order order = orderService.createOrder(request);
        log.info("订单创建成功，orderId: {}, orderSn: {}", order.getId(), order.getOrderSn());
        return Result.success("订单创建成功", order);
    }

    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateOrder(@PathVariable("id") Long id, @RequestBody Order order) {
        log.info("更新订单，orderId: {}", id);
        order.setId(id);
        boolean result = orderService.updateById(order);
        if (result) {
            log.info("订单更新成功，orderId: {}", id);
            return Result.success("订单更新成功", true);
        } else {
            log.error("订单更新失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单更新失败");
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteOrder(@PathVariable("id") Long id) {
        log.info("删除订单，orderId: {}", id);
        boolean result = orderService.deleteById(id);
        if (result) {
            log.info("订单删除成功，orderId: {}", id);
            return Result.success("订单删除成功", true);
        } else {
            log.error("订单删除失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单删除失败");
        }
    }

    /**
     * 批量删除订单
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteOrders(@RequestBody List<Long> ids) {
        log.info("批量删除订单，ids: {}", ids);
        boolean result = orderService.batchDelete(ids);
        if (result) {
            log.info("批量删除成功，count: {}", ids.size());
            return Result.success("批量删除成功", true);
        } else {
            log.error("批量删除失败，ids: {}", ids);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "批量删除失败");
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<Boolean> payOrder(
            @PathVariable("id") Long id,
            @RequestParam("paymentType") Integer paymentType) {
        log.info("支付订单，orderId: {}, paymentType: {}", id, paymentType);
        boolean result = orderService.payOrder(id, paymentType);
        if (result) {
            log.info("订单支付成功，orderId: {}", id);
            return Result.success("支付成功", true);
        } else {
            log.error("订单支付失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "支付失败");
        }
    }

    /**
     * 发货
     */
    @PostMapping("/{id}/deliver")
    public Result<Boolean> deliverOrder(@PathVariable("id") Long id) {
        log.info("订单发货，orderId: {}", id);
        boolean result = orderService.deliveryOrder(id);
        if (result) {
            log.info("订单发货成功，orderId: {}", id);
            return Result.success("发货成功", true);
        } else {
            log.error("订单发货失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "发货失败");
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/receive")
    public Result<Boolean> receiveOrder(@PathVariable("id") Long id) {
        log.info("确认收货，orderId: {}", id);
        boolean result = orderService.receiveOrder(id);
        if (result) {
            log.info("订单确认收货成功，orderId: {}", id);
            return Result.success("确认收货成功", true);
        } else {
            log.error("订单确认收货失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "确认收货失败");
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable("id") Long id) {
        log.info("取消订单，orderId: {}", id);
        boolean result = orderService.cancelOrder(id);
        if (result) {
            log.info("订单取消成功，orderId: {}", id);
            return Result.success("订单取消成功", true);
        } else {
            log.error("订单取消失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "订单取消失败");
        }
    }
}
