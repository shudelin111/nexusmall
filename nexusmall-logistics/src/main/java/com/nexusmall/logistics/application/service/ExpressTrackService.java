package com.nexusmall.logistics.application.service;

import com.nexusmall.logistics.domain.entity.LogisticsTrack;

import java.util.List;

/**
 * 第三方物流查询服务接口
 * <p>
 * 业界标准：
 * - 支持多家快递公司API对接（快递鸟、快递100）
 * - 统一的查询接口
 * - 自动同步物流轨迹到数据库
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface ExpressTrackService {

    /**
     * 查询物流轨迹（从第三方API）
     * <p>
     * 业界标准流程：
     * 1. 调用第三方API查询轨迹
     * 2. 解析返回数据
     * 3. 同步到本地数据库
     * 4. 返回轨迹列表
     * </p>
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @return 物流轨迹列表（按时间倒序）
     */
    List<LogisticsTrack> queryExpressTrack(String expressCompany, String expressNo);

    /**
     * 订阅物流轨迹（用于实时推送）
     * <p>
     * 业界标准：
     * - 订阅后，第三方会在状态变更时主动推送
     * - 适合高频查询的场景
     * </p>
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @param callbackUrl    回调地址（第三方推送至此URL）
     * @return 是否订阅成功
     */
    boolean subscribeExpressTrack(String expressCompany, String expressNo, String callbackUrl);

    /**
     * 取消订阅物流轨迹
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @return 是否取消成功
     */
    boolean unsubscribeExpressTrack(String expressCompany, String expressNo);
}
