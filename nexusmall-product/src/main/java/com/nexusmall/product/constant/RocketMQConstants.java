package com.nexusmall.product.constant;

/**
 * RocketMQ 消息常量（Product 服务）
 */
public class RocketMQConstants {

    /**
     * 库存 Topic
     */
    public static final String STOCK_TOPIC = "stock-topic";

    /**
     * 库存扣减成功消息 Tag
     */
    public static final String STOCK_DECREASED_TAG = "stock-decreased";

    /**
     * 消费者组名
     */
    public static final String STOCK_CONSUMER_GROUP = "stock-consumer-group";

}
