-- =====================================================
-- NexusMall Behavior Service - 用户行为日志服务数据库表结构
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_behavior` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `nexusmall_behavior`;

-- ------------------------------
-- 用户行为日志表（MySQL）
-- ------------------------------
DROP TABLE IF EXISTS `user_behavior_log`;

CREATE TABLE `user_behavior_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `user_name` VARCHAR(64) DEFAULT NULL COMMENT '用户名（冗余字段）',
    `behavior_type` VARCHAR(32) NOT NULL COMMENT '行为类型（如：PLACE_ORDER-下单）',
    `behavior_desc` VARCHAR(255) DEFAULT NULL COMMENT '行为描述',
    `object_id` BIGINT(20) DEFAULT NULL COMMENT '业务对象 ID（如商品 ID、订单 ID）',
    `object_type` VARCHAR(32) DEFAULT NULL COMMENT '业务对象类型（如：product_id、order_id）',
    `object_name` VARCHAR(128) DEFAULT NULL COMMENT '业务对象名称（冗余字段）',
    `extra_data` TEXT DEFAULT NULL COMMENT '额外信息（JSON 格式）',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
    `user_agent` TEXT DEFAULT NULL COMMENT 'User-Agent',
    `occur_time` DATETIME NOT NULL COMMENT '行为发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_behavior_type` (`behavior_type`),
    KEY `idx_object_id` (`object_id`),
    KEY `idx_occur_time` (`occur_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- ------------------------------
-- 测试数据
-- ------------------------------
INSERT INTO `user_behavior_log` (`user_id`, `user_name`, `behavior_type`, `behavior_desc`, `object_id`, `object_type`, `object_name`, `extra_data`, `ip_address`, `user_agent`, `occur_time`) VALUES
(1, '张三', 'BROWSE_PRODUCT', '浏览商品', 1001, 'product_id', 'iPhone 15 Pro', '{"category":"手机","price":7999}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(1, '张三', 'ADD_TO_CART', '加入购物车', 1001, 'product_id', 'iPhone 15 Pro', '{"quantity":1,"sku":"IP15P-256G-BLACK"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(1, '张三', 'PLACE_ORDER', '下单购买', 2001, 'order_id', '订单#202603250001', '{"amount":7999,"payment":"wechat"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW());
