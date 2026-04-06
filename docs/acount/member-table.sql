-- =====================================================
-- NexusMall Member Service - 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 创建数据库
-- 2. 会员档案管理（昵称/头像/生日/性别）
-- 3. 收货地址管理
-- 4. 会员等级/积分/成长值管理
-- 5. 与 Auth 模块通过 user_id 关联
-- =====================================================

-- ============================================
-- 0. 创建数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `nexusmall_member` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_member`;

-- ============================================
-- 1. 会员信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_member` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（关联sys_user.id）',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `member_level_id` BIGINT(20) DEFAULT 1 COMMENT '会员等级ID',
    `growth_point` INT(11) DEFAULT 0 COMMENT '成长值',
    `integration` INT(11) DEFAULT 0 COMMENT '积分',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '用户ID唯一索引',
    KEY `idx_phone` (`phone`) COMMENT '手机号索引',
    KEY `idx_member_level_id` (`member_level_id`) COMMENT '会员等级索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员信息表';

-- ============================================
-- 2. 会员等级表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_member_level` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name` VARCHAR(64) NOT NULL COMMENT '等级名称',
    `growth_point_threshold` INT(11) NOT NULL COMMENT '所需成长值',
    `discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率（0.95=95折）',
    `free_shipping_threshold` DECIMAL(10,2) DEFAULT 0.00 COMMENT '免运费门槛',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_growth_point_threshold` (`growth_point_threshold`) COMMENT '成长值阈值索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 初始化默认会员等级（参考淘宝/京东标准）
INSERT INTO `ums_member_level` (`level_name`, `growth_point_threshold`, `discount`, `free_shipping_threshold`, `description`) VALUES
('普通会员', 0, 1.00, 99.00, '新用户默认等级'),
('黄金会员', 5000, 0.95, 50.00, '年消费满5000元，享受95折优惠'),
('铂金会员', 20000, 0.90, 0.00, '年消费满20000元，享受9折优惠，全场包邮'),
('钻石会员', 100000, 0.85, 0.00, '年消费满100000元，享受85折优惠，全场包邮，专属客服');

-- ============================================
-- 3. 会员收货地址表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_member_receive_address` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `post_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
    `province` VARCHAR(32) NOT NULL COMMENT '省份/直辖市',
    `city` VARCHAR(32) NOT NULL COMMENT '城市',
    `region` VARCHAR(32) NOT NULL COMMENT '区',
    `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址（街道）',
    `default_status` TINYINT(1) DEFAULT 0 COMMENT '是否为默认地址：0=否，1=是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员收货地址表';

-- ============================================
-- 4. 成长值变化历史记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_growth_change_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `change_type` TINYINT(1) NOT NULL COMMENT '变化类型：0=增加，1=减少',
    `change_count` INT(11) NOT NULL COMMENT '变化数量',
    `source_type` VARCHAR(64) NOT NULL COMMENT '来源类型：ORDER(订单)/REVIEW(评价)/SIGN_IN(签到)',
    `source_id` BIGINT(20) DEFAULT NULL COMMENT '来源ID（如订单ID）',
    `note` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长值变化历史记录表';

-- ============================================
-- 5. 积分变化历史记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_integration_change_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `change_type` TINYINT(1) NOT NULL COMMENT '变化类型：0=增加，1=减少',
    `change_count` INT(11) NOT NULL COMMENT '变化数量',
    `source_type` VARCHAR(64) NOT NULL COMMENT '来源类型：ORDER(订单)/COUPON(优惠券)/REFUND(退款)',
    `source_id` BIGINT(20) DEFAULT NULL COMMENT '来源ID',
    `note` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变化历史记录表';

-- ============================================
-- 6. 积分兑换记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `ums_integration_consume_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `integration` INT(11) NOT NULL COMMENT '消耗积分数量',
    `consume_type` VARCHAR(32) NOT NULL COMMENT '兑换类型：COUPON(优惠券)/PRODUCT(商品)/CASH(现金)',
    `object_id` BIGINT(20) DEFAULT NULL COMMENT '兑换对象 ID（如优惠券ID/商品ID）',
    `object_name` VARCHAR(128) DEFAULT NULL COMMENT '兑换对象名称',
    `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '抵扣金额（如果是现金兑换）',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=待处理，1=已完成，2=已取消',
    `note` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- ============================================
-- 索引说明：
-- 1. ums_member.uk_user_id: 保证一个用户只能有一个会员档案
-- 2. ums_member.idx_phone: 支持按手机号快速查询会员
-- 3. ums_member_receive_address.idx_member_id: 支持查询用户的所有收货地址
-- 4. ums_integration_consume_record.idx_member_id: 支持查询用户的兑换记录
-- ============================================
