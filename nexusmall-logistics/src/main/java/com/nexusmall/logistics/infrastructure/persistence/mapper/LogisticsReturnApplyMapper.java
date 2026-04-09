package com.nexusmall.logistics.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退货申?Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface LogisticsReturnApplyMapper extends BaseMapper<LogisticsReturnApply> {

    /**
     * 根据订单编号查询退货申请列?
     *
     * @param orderSn 订单编号
     * @return 退货申请列?
     */
    List<LogisticsReturnApply> selectByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 根据会员ID查询退货申请列?
     *
     * @param memberId 会员ID
     * @return 退货申请列?
     */
    List<LogisticsReturnApply> selectByMemberId(@Param("memberId") Long memberId);
}
