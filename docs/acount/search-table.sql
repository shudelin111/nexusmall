-- =====================================================
-- NexusMall Search Service - 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 搜索历史记录
-- 2. 热门搜索词统计
-- 3. 搜索建议/自动补全
-- 注意：商品全文搜索使用 Elasticsearch，MySQL 仅存储辅助数据
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_search` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_search`;

-- ============================================
-- 1. 搜索历史记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `search_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID（0表示匿名用户）',
    `keyword` VARCHAR(128) NOT NULL COMMENT '搜索关键词',
    `search_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '搜索类型：1=商品，2=店铺，3=文章',
    `result_count` INT(11) DEFAULT 0 COMMENT '搜索结果数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
    KEY `idx_keyword` (`keyword`) COMMENT '关键词索引',
    KEY `idx_create_time` (`create_time`) COMMENT '搜索时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史记录表';

-- ============================================
-- 2. 热门搜索词统计表
-- ============================================
CREATE TABLE IF NOT EXISTS `hot_search_keyword` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(128) NOT NULL COMMENT '搜索关键词',
    `search_count` INT(11) NOT NULL DEFAULT 0 COMMENT '搜索次数',
    `trend` TINYINT(1) DEFAULT 0 COMMENT '趋势：0=平稳，1=上升，2=下降',
    `rank` INT(11) DEFAULT 0 COMMENT '排名',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword_date` (`keyword`, `stat_date`) COMMENT '关键词+日期唯一索引',
    KEY `idx_stat_date` (`stat_date`) COMMENT '统计日期索引',
    KEY `idx_rank` (`rank`) COMMENT '排名索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门搜索词统计表';

-- ============================================
-- 3. 搜索建议词表
-- ============================================
CREATE TABLE IF NOT EXISTS `search_suggestion` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(128) NOT NULL COMMENT '建议关键词',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '关联分类',
    `brand` VARCHAR(64) DEFAULT NULL COMMENT '关联品牌',
    `priority` INT(11) DEFAULT 0 COMMENT '优先级（越高越靠前）',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_keyword` (`keyword`) COMMENT '关键词索引（支持前缀匹配）',
    KEY `idx_priority` (`priority`) COMMENT '优先级索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索建议词表';

-- 初始化默认搜索建议词
INSERT INTO `search_suggestion` (`keyword`, `category`, `priority`) VALUES
('手机', '电子产品', 100),
('笔记本电脑', '电子产品', 95),
('运动鞋', '服装鞋包', 90),
('护肤品', '美妆个护', 85),
('零食', '食品饮料', 80);

-- ============================================
-- 索引说明：
-- 1. search_history.idx_member_id: 支持查询用户的搜索历史
-- 2. hot_search_keyword.uk_keyword_date: 保证同一天同一关键词只有一条记录
-- 3. search_suggestion.idx_keyword: 支持关键词前缀匹配（用于搜索建议）
-- ============================================
