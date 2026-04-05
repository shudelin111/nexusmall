package com.nexusmall.order.controller;

import com.nexusmall.common.constant.LogMessageConstants;
import com.nexusmall.common.constant.ResponseMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.OrderCreateRequest;
import com.nexusmall.order.vo.OrderQueryRequest;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/")  // Gateway 已通过 /order/** 路由，此处不需要再加前缀
@Tag(name = "订单管理", description = "订单的创建、查询、更新、删除及业务流程操作")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 健康检查接口
     */
    @GetMapping("/ping")
    @Operation(summary = "健康检查", description = "检查订单服务是否正常运行")
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
    @Operation(summary = "根据ID查询订单", description = "根据订单ID查询订单详细信息")
    public Result<Order> getOrderById(
            @Parameter(description = "订单ID", example = "1000", required = true)
            @PathVariable("id") Long id) {
        log.info("查询订单，orderId: {}", id);
        Order order = orderService.getById(id);
        if (order != null) {
            log.info(LogMessageConstants.Order.ORDER_QUERIED, id, order.getOrderSn());
            return Result.success(order);
        } else {
            log.warn("订单不存在，orderId: {}", id);
            return Result.failure(CommonResultCode.NOT_FOUND);
        }
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/sn/{orderSn}")
    @Operation(summary = "根据订单号查询", description = "根据订单编号查询订单详细信息")
    public Result<Order> getOrderByOrderSn(
            @Parameter(description = "订单编号", example = "ORD202604040001", required = true)
            @PathVariable("orderSn") String orderSn) {
        log.info("查询订单，orderSn: {}", orderSn);
        Order order = orderService.getByOrderSn(orderSn);
        if (order != null) {
            log.info(LogMessageConstants.Order.ORDER_QUERIED_BY_SN, orderSn, order.getId());
            return Result.success(order);
        } else {
            log.warn("订单不存在，orderSn: {}", orderSn);
            return Result.failure(CommonResultCode.NOT_FOUND);
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
    @Operation(summary = "创建订单", description = "创建新订单，包含库存扣减和订单生成，使用 Seata 分布式事务保证数据一致性")
    public Result<Order> createOrder(
            @Parameter(description = "订单创建请求", required = true)
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("收到创建订单请求，userId: {}, productId: {}, count: {}", 
                request.getMemberId(), request.getProductId(), request.getCount());
        
        Order order = orderService.createOrder(request);
        log.info(LogMessageConstants.Order.ORDER_CREATED, order.getId(), order.getOrderSn());
        return Result.success(ResponseMessageConstants.Order.CREATE_SUCCESS, order);
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
            log.info(LogMessageConstants.Order.ORDER_UPDATED, id);
            return Result.success(ResponseMessageConstants.Order.UPDATE_SUCCESS, true);
        } else {
            log.error("订单更新失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.ORDER_DELETED, id);
            return Result.success(ResponseMessageConstants.Order.DELETE_SUCCESS, true);
        } else {
            log.error("订单删除失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.BATCH_DELETED, ids.size());
            return Result.success(ResponseMessageConstants.Order.BATCH_DELETE_SUCCESS, true);
        } else {
            log.error("批量删除失败，ids: {}", ids);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.ORDER_PAID, id);
            return Result.success(ResponseMessageConstants.Order.PAY_SUCCESS, true);
        } else {
            log.error("订单支付失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.ORDER_DELIVERED, id);
            return Result.success(ResponseMessageConstants.Order.DELIVER_SUCCESS, true);
        } else {
            log.error("订单发货失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.ORDER_RECEIVED, id);
            return Result.success(ResponseMessageConstants.Order.RECEIVE_SUCCESS, true);
        } else {
            log.error("订单确认收货失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
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
            log.info(LogMessageConstants.Order.ORDER_CANCELLED, id);
            return Result.success(ResponseMessageConstants.Order.CANCEL_SUCCESS, true);
        } else {
            log.error("订单取消失败，orderId: {}", id);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }
}
