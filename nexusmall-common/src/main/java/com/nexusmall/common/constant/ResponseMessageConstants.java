package com.nexusmall.common.constant;

/**
 * API 响应消息常量
 * <p>
 * 统一管理所有 Controller 返回的成功/失败消息
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
public class ResponseMessageConstants {

    private ResponseMessageConstants() {
        // 防止实例化
    }

    /**
     * 订单相关成功消息
     */
    public static class Order {
        
        public static final String CREATE_SUCCESS = "订单创建成功";
        public static final String UPDATE_SUCCESS = "订单更新成功";
        public static final String DELETE_SUCCESS = "订单删除成功";
        public static final String BATCH_DELETE_SUCCESS = "批量删除成功";
        public static final String PAY_SUCCESS = "支付成功";
        public static final String DELIVER_SUCCESS = "发货成功";
        public static final String RECEIVE_SUCCESS = "确认收货成功";
        public static final String CANCEL_SUCCESS = "订单取消成功";

        private Order() {
            // 防止实例化
        }
    }

    /**
     * 商品相关成功消息
     */
    public static class Product {
        
        public static final String ADD_SUCCESS = "商品添加成功";
        public static final String UPDATE_SUCCESS = "商品更新成功";
        public static final String DELETE_SUCCESS = "商品删除成功";
        public static final String STOCK_DECREASE_SUCCESS = "库存扣减成功";
        public static final String STOCK_INCREASE_SUCCESS = "库存增加成功";
        public static final String PUT_ON_SALE_SUCCESS = "商品上架成功";
        public static final String PUT_OFF_SALE_SUCCESS = "商品下架成功";
        public static final String BATCH_DECREASE_STOCK_SUCCESS = "批量扣减库存成功";
        public static final String BATCH_INCREASE_STOCK_SUCCESS = "批量增加库存成功";

        private Product() {
            // 防止实例化
        }
    }

    /**
     * 品牌相关成功消息
     */
    public static class Brand {
        
        public static final String ADD_SUCCESS = "品牌添加成功";
        public static final String UPDATE_SUCCESS = "品牌更新成功";
        public static final String DELETE_SUCCESS = "品牌删除成功";

        private Brand() {
            // 防止实例化
        }
    }

    /**
     * 分类相关成功消息
     */
    public static class Category {
        
        public static final String ADD_SUCCESS = "分类添加成功";
        public static final String UPDATE_SUCCESS = "分类更新成功";
        public static final String DELETE_SUCCESS = "分类删除成功";

        private Category() {
            // 防止实例化
        }
    }
}
