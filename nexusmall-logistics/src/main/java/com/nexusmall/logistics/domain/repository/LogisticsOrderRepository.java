package com.nexusmall.logistics.domain.repository;

import com.nexusmall.logistics.domain.entity.LogisticsOrder;

/**
 * 物流订单仓储接口（领域层）
 * <p>
 * 业界标准：
 * - 接口定义在Domain层
 * - 实现在Infrastructure层
 * - 符合依赖倒置原则（DIP）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsOrderRepository {

    /**
     * 根据ID查询
     *
     * @param id 物流订单ID
     * @return 物流订单
     */
    LogisticsOrder findById(Long id);

    /**
     * 根据订单编号查询
     *
     * @param orderSn 订单编号
     * @return 物流订单
     */
    LogisticsOrder findByOrderSn(String orderSn);

    /**
     * 根据快递单号查询
     *
     * @param expressNo 快递单号
     * @return 物流订单
     */
    LogisticsOrder findByExpressNo(String expressNo);

    /**
     * 保存物流订单
     *
     * @param order 物流订单
     * @return 是否成功
     */
    boolean save(LogisticsOrder order);

    /**
     * 更新物流订单
     *
     * @param order 物流订单
     * @return 是否成功
     */
    boolean update(LogisticsOrder order);

    /**
     * 根据ID删除
     *
     * @param id 物流订单ID
     * @return 是否成功
     */
    boolean deleteById(Long id);
}
