package com.nexusmall.logistics.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;

import java.util.List;

/**
 * 物流轨迹服务接口
 * <p>
 * 业界标准：
 * - 支持轨迹添加
 * - 支持轨迹查询（按时间倒序?
 * - 支持第三方物流API同步
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsTrackService extends IService<LogisticsTrack> {

    /**
     * 根据物流订单ID查询轨迹列表（按时间倒序?
     *
     * @param logisticsOrderId 物流订单ID
     * @return 轨迹列表
     */
    List<LogisticsTrack> listByLogisticsOrderId(Long logisticsOrderId);

    /**
     * 根据快递单号查询轨迹列表（按时间倒序?
     *
     * @param expressNo 快递单?
     * @return 轨迹列表
     */
    List<LogisticsTrack> listByExpressNo(String expressNo);

    /**
     * 添加轨迹
     *
     * @param logisticsOrderId 物流订单ID
     * @param expressNo        快递单?
     * @param trackContent     轨迹内容
     * @param trackLocation    轨迹地点
     * @param trackStatus      轨迹状?
     * @return 是否成功
     */
    boolean addTrack(Long logisticsOrderId, String expressNo, String trackContent,
                     String trackLocation, Integer trackStatus);

    /**
     * 批量添加轨迹（从第三方API同步?
     *
     * @param logisticsOrderId 物流订单ID
     * @param expressNo        快递单?
     * @param tracks           轨迹列表
     * @return 是否成功
     */
    boolean batchAddTracks(Long logisticsOrderId, String expressNo, List<LogisticsTrack> tracks);
}
