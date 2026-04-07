package com.nexusmall.logistics.domain.constants;

/**
 * 物流模块常量类
 * <p>
 * 业界标准：统一管理常量，避免硬编码
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public class LogisticsConstants {

    private LogisticsConstants() {
        throw new IllegalStateException("Constant class");
    }

    // ==================== RocketMQ Topic ====================

    /**
     * Topic常量类
     */
    public static class Topic {
        /**
         * 物流状态变更Topic
         */
        public static final String LOGISTICS_STATUS_CHANGE = "LOGISTICS_STATUS_CHANGE_TOPIC";

        /**
         * 订单发货事件Topic
         */
        public static final String ORDER_SHIP_EVENT = "ORDER_SHIP_EVENT_TOPIC";
    }

    // ==================== RocketMQ Tag ====================

    /**
     * Tag常量类
     */
    public static class Tag {
        /**
         * 物流状态变更Tag
         */
        public static final String STATUS_CHANGE = "STATUS_CHANGE";

        /**
         * 订单发货Tag
         */
        public static final String ORDER_SHIP = "ORDER_SHIP";
    }

    // ==================== RocketMQ Consumer Group ====================

    /**
     * Consumer Group常量类
     */
    public static class ConsumerGroup {
        /**
         * 物流消费者组
         */
        public static final String LOGISTICS_CONSUMER_GROUP = "logistics-consumer-group";
    }

    // ==================== Redis Key 前缀 ====================

    /**
     * 物流订单缓存Key前缀
     */
    public static final String LOGISTICS_ORDER_CACHE_KEY_PREFIX = "logistics:order:";

    /**
     * 快递轨迹缓存Key前缀
     */
    public static final String EXPRESS_TRACK_CACHE_KEY_PREFIX = "logistics:track:";

    // ==================== 业务规则常量 ====================

    /**
     * 默认快递公司配置
     */
    public static class DefaultExpress {
        /**
         * 顺丰快递
         */
        public static final String SF_EXPRESS = "SF";
    }

    /**
     * 物流状态：待发货
     */
    public static final int STATUS_PENDING = 0;

    /**
     * 物流状态：已发货
     */
    public static final int STATUS_SHIPPED = 1;

    /**
     * 物流状态：运输中
     */
    public static final int STATUS_IN_TRANSIT = 2;

    /**
     * 物流状态：已签收
     */
    public static final int STATUS_SIGNED = 3;

    /**
     * 物流状态：异常
     */
    public static final int STATUS_EXCEPTION = 4;

    // ==================== 分页常量 ====================

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
}
