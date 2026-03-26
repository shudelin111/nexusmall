package com.nexusmall.common.constant;

/**
 * 日志消息常量
 * <p>
 * 统一管理所有 Controller/Service 中的日志消息
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
public class LogMessageConstants {

    private LogMessageConstants() {
        // 防止实例化
    }

    /**
     * 订单相关日志消息
     */
    public static class Order {
        
        public static final String ORDER_CREATED = "订单创建成功，orderId: {}, orderSn: {}";
        public static final String ORDER_UPDATED = "订单更新成功，orderId: {}";
        public static final String ORDER_DELETED = "订单删除成功，orderId: {}";
        public static final String BATCH_DELETED = "批量删除成功，count: {}";
        public static final String ORDER_PAID = "订单支付成功，orderId: {}";
        public static final String ORDER_DELIVERED = "订单发货成功，orderId: {}";
        public static final String ORDER_RECEIVED = "订单确认收货成功，orderId: {}";
        public static final String ORDER_CANCELLED = "订单取消成功，orderId: {}";
        public static final String ORDER_QUERIED = "订单查询成功，orderId: {}, orderSn: {}";
        public static final String ORDER_QUERIED_BY_SN = "订单查询成功，orderSn: {}, orderId: {}";

        private Order() {
            // 防止实例化
        }
    }

    /**
     * 商品相关日志消息
     */
    public static class Product {
        
        public static final String PRODUCT_ADDED = "商品添加成功，result: {}";
        public static final String PRODUCT_UPDATED = "商品更新成功，result: {}";
        public static final String PRODUCT_DELETED = "商品删除成功，skuId: {}";
        public static final String STOCK_DECREASED = "库存扣减成功，productId: {}, count: {}";
        public static final String STOCK_INCREASED = "库存增加成功，productId: {}, count: {}";
        public static final String STOCK_CHECKED_SUFFICIENT = "库存检查结果：充足";
        public static final String STOCK_CHECKED_INSUFFICIENT = "库存检查结果：不足";
        public static final String PRODUCT_PUT_ON_SALE = "商品上架成功，skuId: {}";
        public static final String PRODUCT_PUT_OFF_SALE = "商品下架成功，skuId: {}";
        public static final String BATCH_STOCK_DECREASED = "批量扣减库存成功";
        public static final String BATCH_STOCK_INCREASED = "批量增加库存成功";
        public static final String XID_RECEIVED_SUCCESS = "✓ Product 服务成功接收到 XID: {}";
        public static final String XID_NOT_RECEIVED = "✗ Product 服务未接收到 XID！";

        private Product() {
            // 防止实例化
        }
    }

    /**
     * 品牌相关日志消息
     */
    public static class Brand {
        
        public static final String BRAND_ADDED = "品牌添加成功，result: {}";
        public static final String BRAND_UPDATED = "品牌更新成功，result: {}";
        public static final String BRAND_DELETED = "品牌删除成功，id: {}";
        public static final String BATCH_BRANDS_DELETED = "批量删除品牌成功，count: {}";

        private Brand() {
            // 防止实例化
        }
    }

    /**
     * 分类相关日志消息
     */
    public static class Category {
        
        public static final String CATEGORY_ADDED = "分类添加成功，result: {}";
        public static final String CATEGORY_UPDATED = "分类更新成功，result: {}";
        public static final String CATEGORY_DELETED = "分类删除成功，id: {}";
        public static final String BATCH_CATEGORIES_DELETED = "批量删除分类成功，count: {}";

        private Category() {
            // 防止实例化
        }
    }

    /**
     * 用户认证相关日志消息
     */
    public static class Auth {
        
        public static final String USER_LOGGED_IN = "用户登录成功，username: {}";
        public static final String USER_LOGGED_OUT = "用户退出成功，username: {}";

        private Auth() {
            // 防止实例化
        }
    }
}
