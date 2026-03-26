package com.nexusmall.common.constant;

/**
 * RocketMQ 消息常量（公共）
 * <p>
 * 所有服务共享的 Topic、Tag、ConsumerGroup 定义
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
public class MQConstants {

    private MQConstants() {
        // 防止实例化
    }

    /**
     * 用户行为相关常量
     */
    public static class UserBehavior {
        
        /**
         * 用户行为日志 Topic
         */
        public static final String TOPIC = "user-behavior-topic";

        /**
         * 用户行为 Tag
         */
        public static final String TAG = "user-behavior";

        /**
         * 消费者组名
         */
        public static final String CONSUMER_GROUP = "user-behavior-consumer-group";

        /**
         * Kafka Topic（用于大数据分析）
         */
        public static final String KAFKA_TOPIC = "user-behavior-log";

        private UserBehavior() {
            // 防止实例化
        }
    }

    /**
     * 订单相关常量
     */
    public static class Order {
        
        /**
         * 订单 Topic
         */
        public static final String TOPIC = "order-topic";

        /**
         * 订单取消延迟消息 Tag
         */
        public static final String CANCEL_TAG = "order-cancel";

        /**
         * 消费者组名
         */
        public static final String CONSUMER_GROUP = "order-consumer-group";

        /**
         * 延迟消息级别（1-18 级）
         * 1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h
         */
        public static final int DELAY_LEVEL_30MIN = 17; // 30 分钟

        private Order() {
            // 防止实例化
        }
    }

    /**
     * 库存相关常量
     */
    public static class Stock {
        
        /**
         * 库存 Topic
         */
        public static final String TOPIC = "stock-topic";

        /**
         * 库存扣减成功消息 Tag
         */
        public static final String DECREASED_TAG = "stock-decreased";

        /**
         * 消费者组名
         */
        public static final String CONSUMER_GROUP = "stock-consumer-group";

        private Stock() {
            // 防止实例化
        }
    }
}
