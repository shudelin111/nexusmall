-- ====================================================
-- Order 模块数据库初始化脚本
-- 数据库：nexusmall_order
-- ====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_order` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_order`;

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

-- 3. 创建订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `member_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) DEFAULT NULL COMMENT '收货人地址',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    `promotion_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '促销优惠金额',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    `payment_type` TINYINT(4) DEFAULT NULL COMMENT '支付方式：1-微信，2-支付宝，3-银行卡',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4. 创建订单项表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT(20) NOT NULL COMMENT '订单 ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `sku_id` BIGINT(20) NOT NULL COMMENT '商品 SKU ID',
    `sku_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `sku_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
    `quantity` INT(11) NOT NULL DEFAULT 0 COMMENT '购买数量',
    `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_sn` (`order_sn`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 5. 插入测试数据 - 订单
INSERT INTO `order` (`order_sn`, `member_id`, `receiver_name`, `receiver_phone`, `receiver_address`, `total_amount`, `pay_amount`, `freight_amount`, `promotion_amount`, `status`, `payment_type`, `remark`) VALUES
('ORD-2026032400001', 1, '张三', '13800138001', '北京市朝阳区 xx 街道 xx 号', 6988.00, 6988.00, 0.00, 0.00, 1, 1, '请尽快发货'),
('ORD-2026032400002', 2, '李四', '13800138002', '上海市浦东新区 xx 路 xx 号', 4999.00, 4999.00, 10.00, 0.00, 2, 2, '周末送货'),
('ORD-2026032400003', 3, '王五', '13800138003', '广州市天河区 xx 大厦 xx 室', 14998.00, 14998.00, 0.00, 0.00, 3, 1, '企业采购');

-- 6. 插入测试数据 - 订单项
INSERT INTO `order_item` (`order_id`, `order_sn`, `sku_id`, `sku_name`, `sku_price`, `quantity`, `subtotal`) VALUES
(1, 'ORD-2026032400001', 1, '华为 Mate 60 Pro 12GB+512GB', 6988.00, 1, 6988.00),
(2, 'ORD-2026032400002', 2, '小米 14 Pro 16GB+1TB', 4999.00, 1, 4999.00),
(3, 'ORD-2026032400003', 1, '华为 Mate 60 Pro 12GB+512GB', 6988.00, 2, 13976.00),
(3, 'ORD-2026032400003', 3, 'iPhone 15 Pro Max 256GB', 9999.00, 1, 9999.00);
