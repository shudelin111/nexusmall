-- =====================================================
-- NexusMall Cart Database Schema
-- 购物车服务数据库表结构
-- =====================================================
-- 技术标准：
-- 1. InnoDB 引擎 + utf8mb4 字符集
-- 2. 商品快照机制（防止商家改价导致体验问题）
-- 3. 乐观锁版本号（防止并发更新冲突）
-- 4. 审计字段（create_time/update_time自动填充）
-- 5. 索引优化（查询性能保障）
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_cart` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `nexusmall_cart`;

-- =====================================================
-- 购物车项表 (cart_item)
-- =====================================================
-- 核心设计：
-- - Redis Hash 主存储（高性能读写）
-- - MySQL 异步持久化（数据兜底）
-- - 商品快照机制（记录加入时的价格、名称、属性）
-- - 支持批量操作（批量删除/批量选中）
-- =====================================================

DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
    -- ===============================
    -- 主键
    -- ===============================
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    
    -- ===============================
    -- 用户和商品信息
    -- ===============================
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `sku_id` BIGINT(20) NOT NULL COMMENT '商品SKU ID',
    `spu_id` BIGINT(20) NOT NULL COMMENT '商品SPU ID',
    
    -- ===============================
    -- 商品快照字段（业界标准！）
    -- ===============================
    -- 为什么需要快照？
    -- 1. 用户将iPhone加入购物车，价格8999元
    -- 2. 商家第二天涨价到9999元
    -- 3. 如果购物车不存快照，用户看到价格突变 → 体验崩塌！
    -- 4. 甚至可能被怀疑"大数据杀熟" → 法律风险！
    -- ===============================
    `product_name` VARCHAR(256) NOT NULL COMMENT '商品名称快照（加入时）',
    `product_image` VARCHAR(512) DEFAULT NULL COMMENT '商品主图快照',
    `snapshot_price` DECIMAL(10,2) NOT NULL COMMENT '加入时价格快照（永不改变）',
    `snapshot_attrs` TEXT DEFAULT NULL COMMENT '商品属性快照JSON（如：{"color":"深空灰","storage":"128GB"}）',
    `snapshot_version` INT(11) NOT NULL DEFAULT 0 COMMENT '商品快照版本号',
    `snapshot_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照创建时间',
    
    -- ===============================
    -- 购物车业务字段
    -- ===============================
    `quantity` INT(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
    `selected` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中：0-否 1-是',
    
    -- ===============================
    -- 库存和限购信息（冗余字段，提升查询性能）
    -- ===============================
    `stock_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '库存状态：0-无货 1-有货 2-预售',
    `max_purchase_limit` INT(11) DEFAULT NULL COMMENT '单次购买上限（null表示无限制）',
    
    -- ===============================
    -- 审计字段
    -- ===============================
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- ===============================
    -- 主键和索引
    -- ===============================
    PRIMARY KEY (`id`),
    
    -- 唯一索引：同一用户的同一SKU只能有一条记录
    UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
    
    -- 普通索引：加速按用户查询
    KEY `idx_user_id` (`user_id`),
    
    -- 普通索引：加速按SKU查询（用于库存变更时批量失效缓存）
    KEY `idx_sku_id` (`sku_id`),
    
    -- 普通索引：加速按SPU查询（用于商品下架时批量处理）
    KEY `idx_spu_id` (`spu_id`),
    
    -- 复合索引：加速查询已选中的商品（结算时使用）
    KEY `idx_user_selected` (`user_id`, `selected`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='购物车项表（Redis主存储+MySQL异步持久化）';

-- =====================================================
-- 初始化测试数据（可选）
-- =====================================================
-- INSERT INTO `cart_item` (`user_id`, `sku_id`, `spu_id`, `product_name`, `product_image`, 
--                          `snapshot_price`, `snapshot_attrs`, `snapshot_version`, `quantity`, `selected`)
-- VALUES 
--     (1001, 10001, 1001, 'iPhone 15 Pro Max', 'https://example.com/iphone.jpg', 
--      8999.00, '{"color":"深空灰","storage":"256GB"}', 1, 1, 1),
--     (1001, 10002, 1002, 'MacBook Pro 14"', 'https://example.com/macbook.jpg', 
--      14999.00, '{"color":"银色","memory":"16GB","storage":"512GB"}', 1, 1, 1);

-- =====================================================
-- 说明
-- =====================================================
-- 1. 数据存储策略：
--    - Redis Hash：主存储，key格式为 cart:user:{userId}
--    - MySQL：异步持久化，作为数据兜底
-- 
-- 2. 商品快照机制：
--    - snapshot_price：记录加入时的价格，永不改变
--    - product_name/product_image：记录加入时的名称和图片
--    - snapshot_attrs：记录加入时的属性（颜色、尺寸等）
--    - 作用：防止商家改价导致用户体验问题
-- 
-- 3. 并发控制：
--    - 乐观锁 version 字段：防止并发更新冲突
--    - 唯一索引 uk_user_sku：保证同一用户同一SKU只有一条记录
-- 
-- 4. 性能优化：
--    - 索引 idx_user_selected：加速结算时查询已选中商品
--    - 冗余 stock_status/max_purchase_limit：减少跨服务调用
-- 
-- 5. 扩展性：
--    - snapshot_attrs 使用 JSON 格式，支持灵活的商品属性
--    - 预留 stock_status 字段，支持预售模式
-- =====================================================
