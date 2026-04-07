package com.nexusmall.logistics.application.service;

import com.nexusmall.logistics.application.dto.LogisticsStatusChangeEvent;

/**
 * 物流事件发布服务接口
 * <p>
 * 业界标准：
 * - 发布物流状态变更事件到RocketMQ
 * - 实现微服务间的异步通信
 * - 支持事件溯源和审计
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsEventPublisherService {

    /**
     * 发布物流状态变更事件
     * <p>
     * 业界标准流程：
     * 1. 构建事件对象
     * 2. 序列化为JSON
     * 3. 发送到RocketMQ
     * 4. 记录发送日志
     * </p>
     *
     * @param event 物流状态变更事件
     * @return 是否发送成功
     */
    boolean publishStatusChangeEvent(LogisticsStatusChangeEvent event);

    /**
     * 批量发布物流状态变更事件
     * <p>
     * 用于批量更新物流状态的场景
     * </p>
     *
     * @param events 事件列表
     * @return 成功发送的数量
     */
    int batchPublishStatusChangeEvents(java.util.List<LogisticsStatusChangeEvent> events);
}
