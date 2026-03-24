-- ====================================================
-- Product 模块数据库初始化脚本
-- 数据库：nexusmall_product
-- ====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_product` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_product`;

-- 2. 创建 UNDO_LOG 表（Seata 分布式事务需要）
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
  `branch_id` BIGINT(20) NOT NULL COMMENT 'branch transaction id',
  `xid` VARCHAR(100) NOT NULL COMMENT 'global transaction id',
  `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
  `log_status` INT(11) NOT NULL COMMENT '0:normal status,1:defended status',
  `log_created` DATETIME NOT NULL COMMENT 'create datetime',
  `log_modified` DATETIME NOT NULL COMMENT 'modify datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

-- 3. 创建商品表
CREATE TABLE IF NOT EXISTS `product` (
    `sku_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `sku_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
    `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类 ID',
    `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称',
    `brand_id` BIGINT(20) DEFAULT NULL COMMENT '品牌 ID',
    `brand_name` VARCHAR(100) DEFAULT NULL COMMENT '品牌名称',
    `description` TEXT COMMENT '商品描述',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`sku_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 4. 创建商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类 ID，0 为一级分类',
    `level` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级，2-二级，3-三级',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 5. 创建商品品牌表
CREATE TABLE IF NOT EXISTS `brand` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '品牌 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '品牌名称',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '品牌 Logo',
    `description` TEXT COMMENT '品牌描述',
    `first_letter` CHAR(1) DEFAULT NULL COMMENT '首字母',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_first_letter` (`first_letter`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';

-- 6. 插入测试数据 - 品牌
INSERT INTO `brand` (`name`, `logo`, `description`, `first_letter`, `sort_order`, `status`) VALUES
('华为', '/images/brands/huawei.png', '华为技术有限公司', 'H', 1, 1),
('小米', '/images/brands/xiaomi.png', '小米科技有限责任公司', 'X', 2, 1),
('苹果', '/images/brands/apple.png', '苹果公司', 'P', 3, 1);

-- 7. 插入测试数据 - 分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`, `status`) VALUES
('手机数码', 0, 1, 1, 1),
('电脑办公', 0, 1, 2, 1),
('家用电器', 0, 1, 3, 1),
('手机通讯', 1, 2, 1, 1),
('手机配件', 1, 2, 2, 1),
('电脑整机', 2, 2, 1, 1),
('电脑配件', 2, 2, 2, 1);

-- 8. 插入测试数据 - 商品
INSERT INTO `product` (`sku_name`, `price`, `stock`, `category_id`, `category_name`, `brand_id`, `brand_name`, `description`, `status`) VALUES
('华为 Mate 60 Pro 12GB+512GB', 6988.00, 1000, 1, '手机数码', 1, '华为', '华为旗舰手机，搭载麒麟 9000S 处理器', 1),
('小米 14 Pro 16GB+1TB', 4999.00, 2000, 1, '手机数码', 2, '小米', '小米旗舰手机，搭载骁龙 8 Gen 3 处理器', 1),
('iPhone 15 Pro Max 256GB', 9999.00, 500, 1, '手机数码', 3, '苹果', '苹果旗舰手机，A17 Pro 芯片', 1);
