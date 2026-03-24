package com.nexusmall.order.service;

import com.nexusmall.order.entity.Order;
import com.nexusmall.order.vo.OrderCreateRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 根据 ID 查询订单
     */
    Order getById(Long id);

    /**
     * 根据订单号查询订单
     */
    Order getByOrderSn(String orderSn);

    /**
     * 查询所有订单
     */
    List<Order> list();

    /**
     * 根据用户 ID 查询订单列表
     */
    List<Order> listByMemberId(Long memberId);

    /**
     * 根据订单状态查询订单列表
     */
    List<Order> listByStatus(Integer status);

    /**
     * 条件查询订单列表
     */
    List<Order> listByCondition(Long memberId, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 创建订单
     */
    Order createOrder(OrderCreateRequest request);

    /**
     * 更新订单
     */
    boolean updateById(Order order);

    /**
     * 删除订单
     */
    boolean deleteById(Long id);

    /**
     * 批量删除订单
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 支付订单
     */
    boolean payOrder(Long id, Integer paymentType);

    /**
     * 发货
     */
    boolean deliveryOrder(Long id);

    /**
     * 确认收货
     */
    boolean receiveOrder(Long id);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long id);
}
