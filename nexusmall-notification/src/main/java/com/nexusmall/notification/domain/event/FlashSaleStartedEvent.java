package com.nexusmall.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀活动开始领域事件
 * <p>
 * 业界标准（Event-Driven Architecture）：
 * - Promotion 服务在秒杀活动开始前发布此事件
 * - Notification 服务监听并批量推送提醒消息
 * - 支持高并发场景下的异步通知
 * - 符合发布-订阅（Pub/Sub）模式
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleStartedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动 ID
     */
    private Long flashSaleId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 商品 ID 列表
     */
    private java.util.List<Long> productIds;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 折扣力度（例如：5.0 表示 5 折）
     */
    private java.math.BigDecimal discountRate;

    /**
     * 发布时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 事件ID（用于幂等性校验）
     */
    private String eventId;
}
