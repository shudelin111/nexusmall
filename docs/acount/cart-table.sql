-- =====================================================
-- NexusMall 购物车模块 - 数据库表结构
-- =====================================================
-- 说明：
-- 1. 购物车采用 Redis Hash 主存储 + MySQL 异步持久化
-- 2. 商品快照机制：记录加入时的价格、名称、属性，防止商家改价导致用户体验问题
-- 3. 支持匿名购物车合并：未登录用户使用 temp_cart_id，登录后合并到正式账户
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_cart` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `nexusmall_cart`;

-- ===============================
-- 购物车项表
-- ===============================
CREATE TABLE `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sku_id` BIGINT NOT NULL COMMENT '商品SKU ID',
  `spu_id` BIGINT NOT NULL COMMENT '商品SPU ID',
  
  -- 商品快照字段(关键！)
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称快照(加入时)',
  `product_image` VARCHAR(500) COMMENT '商品主图快照',
  `snapshot_price` DECIMAL(10,2) NOT NULL COMMENT '加入时价格快照(永不改变)',
  `snapshot_attrs` JSON COMMENT '商品属性快照JSON(color/storage等)',
  `snapshot_version` INT NOT NULL DEFAULT 0 COMMENT '商品快照版本号',
  `snapshot_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照创建时间',
  
  -- 购物车业务字段
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中: 0-否 1-是',
  
  -- 审计字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`) COMMENT '用户+SKU唯一索引(幂等性保证)',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引(快速查询购物车)',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引(定时清理)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- ===============================
-- 插入测试数据
-- ===============================
INSERT INTO `cart_item` (`user_id`, `sku_id`, `spu_id`, `product_name`, `product_image`, `snapshot_price`, `snapshot_attrs`, `quantity`, `selected`) 
VALUES 
(1, 1001, 101, 'iPhone 15 Pro', 'https://example.com/iphone15.jpg', 8999.00, '{"color": "深空灰", "storage": "128GB"}', 2, 1),
(1, 1002, 102, 'MacBook Air M3', 'https://example.com/macbook.jpg', 7999.00, '{"color": "银色", "memory": "16GB"}', 1, 1);
