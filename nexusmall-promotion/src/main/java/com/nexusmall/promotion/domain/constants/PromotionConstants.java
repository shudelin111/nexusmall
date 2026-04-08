package com.nexusmall.promotion.domain.constants;

/**
 * 营销模块常量类
 * <p>
 * 业界标准：统一管理常量，避免硬编码
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public class PromotionConstants {

    private PromotionConstants() {
        throw new IllegalStateException("Constant class");
    }

    // ==================== Redis Key 前缀 ====================

    /**
     * 秒杀库存Key前缀
     */
    public static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";

    /**
     * 秒杀用户购买记录Key前缀
     */
    public static final String SECKILL_USER_KEY_PREFIX = "seckill:user:";

    /**
     * 秒杀分布式锁Key前缀
     */
    public static final String SECKILL_LOCK_KEY_PREFIX = "seckill:lock:";

    /**
     * 优惠券缓存Key前缀
     */
    public static final String COUPON_CACHE_KEY_PREFIX = "coupon:";

    /**
     * 活动缓存Key前缀
     */
    public static final String ACTIVITY_CACHE_KEY_PREFIX = "activity:";

    // ==================== RocketMQ Topic ====================

    /**
     * 秒杀订单Topic
     */
    public static final String TOPIC_SECKILL_ORDER = "SECKILL_ORDER_TOPIC";

    /**
     * 优惠券到期Topic
     */
    public static final String TOPIC_COUPON_EXPIRE = "COUPON_EXPIRE_TOPIC";

    /**
     * 统计数据聚合Topic
     */
    public static final String TOPIC_STATISTICS_AGGREGATION = "STATISTICS_AGGREGATION_TOPIC";

    // ==================== RocketMQ Tag ====================

    /**
     * 秒杀订单Tag
     */
    public static final String TAG_SECKILL_ORDER_CREATE = "SECKILL_ORDER_CREATE";

    // ==================== 业务规则常量 ====================

    /**
     * 默认每人限购数量
     */
    public static final int DEFAULT_PER_LIMIT = 1;

    /**
     * Redis库存缓存过期时间（小时）
     */
    public static final long REDIS_STOCK_CACHE_EXPIRE_HOURS = 24;

    /**
     * 用户购买记录过期时间（小时）
     */
    public static final long USER_PURCHASE_RECORD_EXPIRE_HOURS = 24;

    /**
     * 分布式锁等待时间（秒）
     */
    public static final long LOCK_WAIT_TIME_SECONDS = 5;

    /**
     * 分布式锁 lease 时间（秒）
     */
    public static final long LOCK_LEASE_TIME_SECONDS = 10;

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
