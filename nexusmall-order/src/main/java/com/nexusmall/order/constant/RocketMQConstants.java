package com.nexusmall.order.constant;

/**
 * RocketMQ 消息常量（Order 服务）
 */
public class RocketMQConstants {

    /**
     * 订单 Topic
     */
    public static final String ORDER_TOPIC = "order-topic";

    /**
     * 订单取消延迟消息 Tag
     */
    public static final String ORDER_CANCEL_TAG = "order-cancel";

    /**
     * 库存扣减成功消息 Tag
     */
    public static final String STOCK_DECREASED_TAG = "stock-decreased";

    /**
     * 延迟消息级别（1-18 级）
     * 1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h
     */
    public static final int DELAY_LEVEL_30MIN = 17; // 30 分钟

    /**
     * 消费者组名
     */
    public static final String ORDER_CONSUMER_GROUP = "order-consumer-group";

}
