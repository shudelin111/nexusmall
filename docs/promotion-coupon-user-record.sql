-- =====================================================
-- Promotion 模块 - 用户优惠券领取记录表
-- 第一阶段 MVP 核心功能
-- =====================================================

CREATE TABLE IF NOT EXISTS `promotion_coupon_user_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `coupon_id` BIGINT(20) NOT NULL COMMENT '优惠券ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `coupon_name` VARCHAR(200) DEFAULT NULL COMMENT '优惠券名称（冗余字段）',
    `coupon_type` TINYINT(4) NOT NULL COMMENT '优惠类型：1-满减 2-折扣 3-立减',
    `value` DECIMAL(10, 2) NOT NULL COMMENT '面值/折扣率',
    `min_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '最低消费金额',
    `max_discount` DECIMAL(10, 2) DEFAULT NULL COMMENT '最高优惠金额',
    `scope` TINYINT(4) DEFAULT 0 COMMENT '使用范围：0-全场 1-指定分类 2-指定商品',
    `scope_data` TEXT COMMENT '适用范围JSON',
    `valid_start` DATETIME NOT NULL COMMENT '有效期开始时间',
    `valid_end` DATETIME NOT NULL COMMENT '有效期结束时间',
    `use_status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '使用状态：0-未使用 1-已使用 2-已过期 3-已锁定',
    `order_id` BIGINT(20) DEFAULT NULL COMMENT '使用的订单ID',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_coupon_id` (`coupon_id`) COMMENT '优惠券ID索引',
    KEY `idx_use_status` (`use_status`) COMMENT '使用状态索引',
    KEY `idx_order_id` (`order_id`) COMMENT '订单ID索引',
    KEY `idx_receive_time` (`receive_time`) COMMENT '领取时间索引',
    KEY `idx_valid_end` (`valid_end`) COMMENT '有效期结束时间索引（用于过期扫描）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券领取记录表';

-- =====================================================
-- 索引说明：
-- 1. idx_user_id: 查询用户的优惠券列表
-- 2. idx_coupon_id: 统计某优惠券被多少人领取
-- 3. idx_use_status: 按状态筛选优惠券
-- 4. idx_order_id: 查询订单使用的优惠券（用于退款回退）
-- 5. idx_receive_time: 按领取时间排序
-- 6. idx_valid_end: 定时任务扫描即将过期的优惠券
-- =====================================================

-- =====================================================
-- 示例数据（测试用）
-- =====================================================

-- INSERT INTO `promotion_coupon_user_record` 
-- (`coupon_id`, `user_id`, `coupon_name`, `coupon_type`, `value`, `min_amount`, `max_discount`, `scope`, `valid_start`, `valid_end`, `use_status`) 
-- VALUES 
-- (1, 1001, '双11满减券', 1, 50.00, 200.00, 50.00, 0, '2026-04-07 00:00:00', '2026-04-30 23:59:59', 0),
-- (1, 1002, '双11满减券', 1, 50.00, 200.00, 50.00, 0, '2026-04-07 00:00:00', '2026-04-30 23:59:59', 0),
-- (2, 1001, '新人专享券', 3, 20.00, 0.00, 20.00, 0, '2026-04-07 00:00:00', '2026-05-07 23:59:59', 0);
