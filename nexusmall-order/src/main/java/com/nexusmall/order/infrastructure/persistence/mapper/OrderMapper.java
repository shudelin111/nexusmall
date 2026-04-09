package com.nexusmall.order.infrastructure.persistence.mapper;

import com.nexusmall.order.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据 ID 查询订单
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订?
     */
    Order selectByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 查询所有订?
     */
    List<Order> list();

    /**
     * 根据用户 ID 查询订单列表
     */
    List<Order> listByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据订单状态查询订单列?
     */
    List<Order> listByStatus(@Param("status") Integer status);

    /**
     * 条件查询订单列表
     */
    List<Order> listByCondition(
            @Param("memberId") Long memberId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 根据 ID 更新订单
     */
    int updateById(Order order);

    /**
     * 根据 ID 删除订单
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除订单
     */
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 更新订单状?
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status
    );

    /**
     * 支付订单
     */
    int payOrder(
            @Param("id") Long id,
            @Param("paymentType") Integer paymentType,
            @Param("paymentTime") LocalDateTime paymentTime
    );

    /**
     * 发货
     */
    int deliveryOrder(
            @Param("id") Long id,
            @Param("deliveryTime") LocalDateTime deliveryTime
    );

    /**
     * 确认收货
     */
    int receiveOrder(
            @Param("id") Long id,
            @Param("receiveTime") LocalDateTime receiveTime
    );

    /**
     * 取消订单
     */
    int cancelOrder(@Param("id") Long id);
}
