-- =====================================================
-- NexusMall 库存服务数据库初始化脚本
-- =====================================================
-- 数据库名称：nexusmall_inventory
-- 用途：库存管理（SKU库存、库存流水）
-- 作者：shudl
-- 日期：2026-04-06
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_inventory` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `nexusmall_inventory`;

-- =====================================================
-- 1. SKU库存表
-- =====================================================
DROP TABLE IF EXISTS `inventory_sku_stock`;
CREATE TABLE `inventory_sku_stock` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT(20) NOT NULL COMMENT '商品SKU ID',
    `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
    `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '可售库存数量',
    `locked_stock` INT(11) NOT NULL DEFAULT 0 COMMENT '锁定库存数量（已下单未支付）',
    `actual_stock` INT(11) NOT NULL DEFAULT 0 COMMENT '实际库存（可售 + 锁定）',
    `warning_threshold` INT(11) NOT NULL DEFAULT 10 COMMENT '库存预警阈值',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`) COMMENT 'SKU+仓库唯一索引',
    KEY `idx_sku_id` (`sku_id`) COMMENT 'SKU ID索引',
    KEY `idx_warehouse_id` (`warehouse_id`) COMMENT '仓库ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU库存表';

-- =====================================================
-- 2. 库存流水表
-- =====================================================
DROP TABLE IF EXISTS `inventory_stock_log`;
CREATE TABLE `inventory_stock_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT(20) NOT NULL COMMENT '商品SKU ID',
    `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
    `change_quantity` INT(11) NOT NULL COMMENT '变更数量（正数=入库，负数=出库）',
    `before_stock` INT(11) NOT NULL COMMENT '变更前库存',
    `after_stock` INT(11) NOT NULL COMMENT '变更后库存',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：PURCHASE_IN(采购入库)、ORDER_DEDUCT(订单扣减)、ORDER_CANCEL(订单取消回滚)、MANUAL_ADJUST(手动调整)',
    `business_sn` VARCHAR(100) DEFAULT NULL COMMENT '业务单号（订单号/采购单号等）',
    `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`) COMMENT 'SKU ID索引',
    KEY `idx_warehouse_id` (`warehouse_id`) COMMENT '仓库ID索引',
    KEY `idx_business_sn` (`business_sn`) COMMENT '业务单号索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- =====================================================
-- 3. 插入测试数据
-- =====================================================
INSERT INTO `inventory_sku_stock` (`sku_id`, `warehouse_id`, `stock`, `locked_stock`, `actual_stock`, `warning_threshold`, `version`) VALUES
(1001, 1, 100, 0, 100, 10, 0),
(1002, 1, 50, 0, 50, 5, 0),
(1003, 1, 200, 0, 200, 20, 0);
