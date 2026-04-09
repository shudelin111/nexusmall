package com.nexusmall.common.constants;

/**
 * 消息队列 Topic 常量
 * <p>
 * 统一管理所?Kafka/RocketMQ ?Topic 名称
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
public class MQTopicConstants {

    private MQTopicConstants() {
        // 防止实例?
    }

    /**
     * 商品索引同步 Topic
     */
    public static final String PRODUCT_INDEX_SYNC = "product-index-sync";

    /**
     * 订单创建 Topic
     */
    public static final String ORDER_CREATED = "order-created";

    /**
     * 订单支付 Topic
     */
    public static final String ORDER_PAID = "order-paid";

    /**
     * 库存扣减 Topic
     */
    public static final String STOCK_DECREASED = "stock-decreased";

    /**
     * 用户行为日志 Topic
     */
    public static final String USER_BEHAVIOR_LOG = "user-behavior-log";

    /**
     * 应用日志收集 Topic（用?ELK?
     */
    public static final String APP_LOGS = "app-logs";
}
