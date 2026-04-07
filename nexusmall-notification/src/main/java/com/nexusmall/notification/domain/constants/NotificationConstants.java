package com.nexusmall.notification.domain.constants;

/**
 * 通知服务常量
 * <p>
 * 业界标准：
 * - 统一常量管理
 * - 避免硬编码
 * - 便于维护
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public final class NotificationConstants {

    private NotificationConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * RocketMQ Topic 常量
     */
    public static final class Topics {
        /** 订单事件 Topic */
        public static final String ORDER_EVENT = "order-event-topic";
        /** 支付事件 Topic */
        public static final String PAYMENT_EVENT = "payment-event-topic";
        /** 促销事件 Topic */
        public static final String PROMOTION_EVENT = "promotion-event-topic";
        /** 会员事件 Topic */
        public static final String MEMBER_EVENT = "member-event-topic";
    }

    /**
     * Consumer Group 常量
     */
    public static final class ConsumerGroups {
        /** 订单通知消费者组 */
        public static final String ORDER_NOTIFICATION = "notification-order-consumer-group";
        /** 支付通知消费者组 */
        public static final String PAYMENT_NOTIFICATION = "notification-payment-consumer-group";
        /** 促销通知消费者组 */
        public static final String PROMOTION_NOTIFICATION = "notification-promotion-consumer-group";
        /** 会员通知消费者组 */
        public static final String MEMBER_NOTIFICATION = "notification-member-consumer-group";
    }

    /**
     * 业务类型常量
     */
    public static final class BusinessType {
        /** 订单 */
        public static final String ORDER = "ORDER";
        /** 支付 */
        public static final String PAYMENT = "PAYMENT";
        /** 促销 */
        public static final String PROMOTION = "PROMOTION";
        /** 优惠券 */
        public static final String COUPON = "COUPON";
        /** 会员 */
        public static final String MEMBER = "MEMBER";
    }

    /**
     * 模板代码常量
     */
    public static final class TemplateCode {
        /** 注册验证码 */
        public static final String REGISTER_CODE = "REGISTER_CODE";
        /** 订单支付成功 */
        public static final String ORDER_PAID = "ORDER_PAID";
        /** 订单发货通知 */
        public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
        /** 订单取消通知 */
        public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
        /** 优惠券到账 */
        public static final String COUPON_RECEIVED = "COUPON_RECEIVED";
        /** 活动开始提醒 */
        public static final String PROMOTION_STARTED = "PROMOTION_STARTED";
    }

    /**
     * 重试配置
     */
    public static final class RetryConfig {
        /** 最大重试次数 */
        public static final int MAX_RETRY_TIMES = 3;
        /** 重试间隔（秒） */
        public static final long RETRY_INTERVAL_SECONDS = 60;
    }

    /**
     * 清理配置
     */
    public static final class CleanupConfig {
        /** 消息保留天数 */
        public static final int MESSAGE_RETENTION_DAYS = 90;
        /** 记录保留天数 */
        public static final int RECORD_RETENTION_DAYS = 180;
    }
}
