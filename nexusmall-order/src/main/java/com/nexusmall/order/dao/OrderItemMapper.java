package com.nexusmall.order.dao;

import com.nexusmall.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 根据 ID 查询订单项
     */
    OrderItem selectById(@Param("id") Long id);

    /**
     * 根据订单 ID 查询订单项列表
     */
    List<OrderItem> listByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单项列表
     */
    List<OrderItem> listByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 插入订单项
     */
    int insert(OrderItem orderItem);

    /**
     * 批量插入订单项
     */
    int batchInsert(@Param("list") List<OrderItem> orderItems);

    /**
     * 根据订单 ID 删除订单项
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量删除订单项
     */
    int batchDelete(@Param("ids") List<Long> ids);
}
