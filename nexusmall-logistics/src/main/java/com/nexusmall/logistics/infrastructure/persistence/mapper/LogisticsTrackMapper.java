package com.nexusmall.logistics.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流轨迹 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface LogisticsTrackMapper extends BaseMapper<LogisticsTrack> {

    /**
     * 根据物流订单ID查询轨迹列表（按时间倒序?
     *
     * @param logisticsOrderId 物流订单ID
     * @return 轨迹列表
     */
    List<LogisticsTrack> selectByLogisticsOrderId(@Param("logisticsOrderId") Long logisticsOrderId);

    /**
     * 根据快递单号查询轨迹列表（按时间倒序?
     *
     * @param expressNo 快递单?
     * @return 轨迹列表
     */
    List<LogisticsTrack> selectByExpressNo(@Param("expressNo") String expressNo);
}
