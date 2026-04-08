package com.nexusmall.logistics.domain.repository;

import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;

import java.util.List;

/**
 * 退货申请仓储接口（领域层）
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsReturnApplyRepository {

    /**
     * 根据ID查询
     */
    LogisticsReturnApply findById(Long id);

    /**
     * 根据订单编号查询
     */
    List<LogisticsReturnApply> findByOrderSn(String orderSn);

    /**
     * 根据会员ID查询
     */
    List<LogisticsReturnApply> findByMemberId(Long memberId);

    /**
     * 保存退货申请
     */
    boolean save(LogisticsReturnApply apply);

    /**
     * 更新退货申请
     */
    boolean update(LogisticsReturnApply apply);

    /**
     * 根据ID删除
     */
    boolean deleteById(Long id);
}
