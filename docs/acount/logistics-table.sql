-- =====================================================
-- NexusMall Logistics Service - 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 物流订单管理
-- 2. 物流轨迹跟踪
-- 3. 运费计算规则
-- 4. 仓库管理
-- 5. 发货/退货管理
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_logistics` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_logistics`;

-- ============================================
-- 1. 物流订单表
-- ============================================
CREATE TABLE IF NOT EXISTS `logistics_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `warehouse_id` BIGINT(20) DEFAULT NULL COMMENT '发货仓库ID',
    `express_company` VARCHAR(64) NOT NULL COMMENT '快递公司',
    `express_no` VARCHAR(64) NOT NULL COMMENT '快递单号',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(512) NOT NULL COMMENT '收货地址',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '物流状态：0=待发货，1=已发货，2=运输中，3=已签收，4=异常',
    `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '签收时间',
    `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`) COMMENT '订单编号唯一索引',
    UNIQUE KEY `uk_express_no` (`express_no`) COMMENT '快递单号唯一索引',
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
    KEY `idx_status` (`status`) COMMENT '物流状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流订单表';

-- ============================================
-- 2. 物流轨迹表
-- ============================================
CREATE TABLE IF NOT EXISTS `logistics_track` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `logistics_order_id` BIGINT(20) NOT NULL COMMENT '物流订单ID',
    `express_no` VARCHAR(64) NOT NULL COMMENT '快递单号',
    `track_time` DATETIME NOT NULL COMMENT '轨迹时间',
    `track_content` VARCHAR(512) NOT NULL COMMENT '轨迹内容',
    `track_location` VARCHAR(256) DEFAULT NULL COMMENT '轨迹地点',
    `track_status` TINYINT(1) DEFAULT NULL COMMENT '轨迹状态：1=已揽件，2=运输中，3=派送中，4=已签收',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_logistics_order_id` (`logistics_order_id`) COMMENT '物流订单ID索引',
    KEY `idx_express_no` (`express_no`) COMMENT '快递单号索引',
    KEY `idx_track_time` (`track_time`) COMMENT '轨迹时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

-- ============================================
-- 3. 仓库表
-- ============================================
CREATE TABLE IF NOT EXISTS `logistics_warehouse` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(32) NOT NULL COMMENT '仓库编码',
    `province` VARCHAR(32) NOT NULL COMMENT '省份',
    `city` VARCHAR(32) NOT NULL COMMENT '城市',
    `region` VARCHAR(32) NOT NULL COMMENT '区',
    `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址',
    `contact_person` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_code`) COMMENT '仓库编码唯一索引',
    KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 初始化默认仓库
INSERT INTO `logistics_warehouse` (`warehouse_name`, `warehouse_code`, `province`, `city`, `region`, `detail_address`, `contact_person`, `contact_phone`) VALUES
('华东仓', 'WH_EAST', '上海市', '上海市', '浦东新区', '张江高科技园区XX路XX号', '张三', '13800138000'),
('华北仓', 'WH_NORTH', '北京市', '北京市', '朝阳区', '望京SOHO XX层', '李四', '13800138001'),
('华南仓', 'WH_SOUTH', '广东省', '深圳市', '南山区', '科技园XX大厦', '王五', '13800138002');

-- ============================================
-- 4. 运费模板表
-- ============================================
CREATE TABLE IF NOT EXISTS `logistics_freight_template` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `charge_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '计费方式：1=按重量，2=按体积，3=按件数',
    `first_weight` DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '首重（kg）',
    `first_fee` DECIMAL(10,2) NOT NULL DEFAULT 10.00 COMMENT '首重费用（元）',
    `continued_weight` DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '续重（kg）',
    `continued_fee` DECIMAL(10,2) NOT NULL DEFAULT 5.00 COMMENT '续重费用（元）',
    `free_threshold` DECIMAL(10,2) DEFAULT 99.00 COMMENT '包邮门槛（元）',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费模板表';

-- 初始化默认运费模板
INSERT INTO `logistics_freight_template` (`template_name`, `charge_type`, `first_weight`, `first_fee`, `continued_weight`, `continued_fee`, `free_threshold`) VALUES
('标准运费模板', 1, 1.00, 10.00, 1.00, 5.00, 99.00);

-- ============================================
-- 5. 退货申请表
-- ============================================
CREATE TABLE IF NOT EXISTS `logistics_return_apply` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `return_reason` VARCHAR(256) NOT NULL COMMENT '退货原因',
    `return_description` TEXT DEFAULT NULL COMMENT '退货说明',
    `return_images` VARCHAR(1024) DEFAULT NULL COMMENT '退货凭证图片（JSON数组）',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0=申请中，1=已同意，2=已拒绝，3=已完成',
    `express_company` VARCHAR(64) DEFAULT NULL COMMENT '退货快递公司',
    `express_no` VARCHAR(64) DEFAULT NULL COMMENT '退货快递单号',
    `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_sn` (`order_sn`) COMMENT '订单编号索引',
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
    KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货申请表';

-- ============================================
-- 索引说明：
-- 1. logistics_order.uk_order_sn: 保证订单编号唯一性
-- 2. logistics_order.uk_express_no: 保证快递单号唯一性
-- 3. logistics_track.idx_logistics_order_id: 支持查询物流订单的所有轨迹
-- 4. logistics_warehouse.uk_warehouse_code: 保证仓库编码唯一性
-- 5. logistics_return_apply.idx_order_sn: 支持查询订单的退货申请
-- ============================================
