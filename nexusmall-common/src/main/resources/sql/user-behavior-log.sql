-- =====================================================
-- NexusMall 用户行为日志表结构
-- =====================================================
-- 用途：记录用户在系统中的关键业务行为
-- 存储策略：仅存储关键交易行为（下单、支付、退款等）
--           一般行为（浏览、收藏等）不入库
-- =====================================================

-- 创建用户行为日志表
CREATE TABLE IF NOT EXISTS `user_behavior_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 用户信息
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `user_name` VARCHAR(64) DEFAULT NULL COMMENT '用户名（冗余字段，便于查询）',
    
    -- 行为信息
    `behavior_type` VARCHAR(32) NOT NULL COMMENT '行为类型（如：PLACE_ORDER-下单）',
    `behavior_desc` VARCHAR(128) DEFAULT NULL COMMENT '行为描述',
    
    -- 业务对象信息
    `object_id` BIGINT(20) DEFAULT NULL COMMENT '业务对象 ID（如商品 ID、订单 ID）',
    `object_type` VARCHAR(32) DEFAULT NULL COMMENT '业务对象类型（如：product_id、order_id）',
    `object_name` VARCHAR(128) DEFAULT NULL COMMENT '业务对象名称（冗余字段）',
    
    -- 额外信息
    `extra_data` TEXT DEFAULT NULL COMMENT '额外信息（JSON 格式）',
    
    -- 设备与环境信息
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
    `device_type` VARCHAR(32) DEFAULT NULL COMMENT '设备类型（mobile/pc/app）',
    `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
    `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器',
    
    -- 位置信息
    `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    
    -- 时间信息
    `occur_time` DATETIME NOT NULL COMMENT '行为发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    -- 索引优化
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '按用户 ID 查询',
    KEY `idx_behavior_type` (`behavior_type`) COMMENT '按行为类型统计',
    KEY `idx_object` (`object_type`, `object_id`) COMMENT '按业务对象查询',
    KEY `idx_occur_time` (`occur_time`) COMMENT '按时间范围查询',
    KEY `idx_create_time` (`create_time`) COMMENT '按创建时间查询'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 插入示例数据（用于测试）
INSERT INTO `user_behavior_log` 
(`user_id`, `behavior_type`, `behavior_desc`, `object_id`, `object_type`, `extra_data`, `ip_address`, `occur_time`) 
VALUES 
(1001, 'PLACE_ORDER', '下单购买', 10001, 'order_id', '{"amount": 299.00}', '192.168.1.100', NOW()),
(1002, 'PLACE_ORDER', '下单购买', 10002, 'order_id', '{"amount": 599.00}', '192.168.1.101', NOW());

-- 查询示例
-- SELECT * FROM user_behavior_log ORDER BY create_time DESC LIMIT 10;
