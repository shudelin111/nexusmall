package com.nexusmall.logistics.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 物流订单 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface LogisticsOrderMapper extends BaseMapper<LogisticsOrder> {

    /**
     * 根据订单编号查询物流订单
     *
     * @param orderSn 订单编号
     * @return 物流订单
     */
    LogisticsOrder selectByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 根据快递单号查询物流订单
     *
     * @param expressNo 快递单号
     * @return 物流订单
     */
    LogisticsOrder selectByExpressNo(@Param("expressNo") String expressNo);
}
