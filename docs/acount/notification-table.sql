-- =====================================================
-- NexusMall Notification Service - 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 站内消息管理（系统通知/订单状态/营销活动）
-- 2. 短信发送记录
-- 3. 邮件发送记录
-- 4. 推送通知记录
-- 5. 消息模板管理
-- =====================================================

-- ============================================
-- 0. 创建数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `nexusmall_notification` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_notification`;

-- ============================================
-- 1. 站内消息表
-- ============================================
CREATE TABLE IF NOT EXISTS `notification_message` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `title` VARCHAR(128) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '消息类型：1=系统通知，2=订单状态，3=营销活动，4=优惠券提醒',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '阅读状态：0=未读，1=已读',
    `business_type` VARCHAR(32) DEFAULT NULL COMMENT '业务类型：ORDER/PROMOTION/COUPON',
    `business_id` BIGINT(20) DEFAULT NULL COMMENT '业务ID（如订单ID/活动ID）',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
    KEY `idx_status` (`status`) COMMENT '阅读状态索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ============================================
-- 2. 短信发送记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `sms_send_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `template_code` VARCHAR(64) NOT NULL COMMENT '短信模板代码',
    `template_param` VARCHAR(512) DEFAULT NULL COMMENT '模板参数（JSON格式）',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '短信内容',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0=待发送，1=发送成功，2=发送失败',
    `error_msg` VARCHAR(256) DEFAULT NULL COMMENT '错误信息',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID（用于幂等性控制）',
    `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_id` (`biz_id`) COMMENT '业务ID唯一索引（幂等性）',
    KEY `idx_phone` (`phone`) COMMENT '手机号索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送记录表';

-- ============================================
-- 3. 邮件发送记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `email_send_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `to_email` VARCHAR(128) NOT NULL COMMENT '收件人邮箱',
    `subject` VARCHAR(256) NOT NULL COMMENT '邮件主题',
    `content` TEXT NOT NULL COMMENT '邮件内容（HTML）',
    `template_code` VARCHAR(64) DEFAULT NULL COMMENT '邮件模板代码',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0=待发送，1=发送成功，2=发送失败',
    `error_msg` VARCHAR(256) DEFAULT NULL COMMENT '错误信息',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID（用于幂等性控制）',
    `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_id` (`biz_id`) COMMENT '业务ID唯一索引（幂等性）',
    KEY `idx_to_email` (`to_email`) COMMENT '收件人邮箱索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送记录表';

-- ============================================
-- 4. 推送通知记录表（APP Push/微信模板消息）
-- ============================================
CREATE TABLE IF NOT EXISTS `push_notification_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `platform` TINYINT(1) NOT NULL COMMENT '推送平台：1=APP Push，2=微信小程序，3=微信公众号',
    `title` VARCHAR(128) NOT NULL COMMENT '推送标题',
    `content` VARCHAR(500) NOT NULL COMMENT '推送内容',
    `extra_data` VARCHAR(1024) DEFAULT NULL COMMENT '扩展数据（JSON格式，用于跳转链接等）',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '推送状态：0=待推送，1=推送成功，2=推送失败',
    `error_msg` VARCHAR(256) DEFAULT NULL COMMENT '错误信息',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID（用于幂等性控制）',
    `push_time` DATETIME DEFAULT NULL COMMENT '推送时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_id` (`biz_id`) COMMENT '业务ID唯一索引（幂等性）',
    KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送通知记录表';

-- ============================================
-- 5. 消息模板表
-- ============================================
CREATE TABLE IF NOT EXISTS `message_template` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(64) NOT NULL COMMENT '模板名称',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板代码（唯一标识）',
    `channel` TINYINT(1) NOT NULL COMMENT '渠道：1=短信，2=邮件，3=APP Push，4=微信',
    `template_content` TEXT NOT NULL COMMENT '模板内容（支持占位符）',
    `variables` VARCHAR(512) DEFAULT NULL COMMENT '变量列表（JSON格式）',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`) COMMENT '模板代码唯一索引',
    KEY `idx_channel` (`channel`) COMMENT '渠道索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 初始化默认消息模板
INSERT INTO `message_template` (`template_name`, `template_code`, `channel`, `template_content`, `variables`) VALUES
('注册验证码', 'REGISTER_CODE', 1, '您的验证码是：${code}，有效期${minutes}分钟', '["code", "minutes"]'),
('订单支付成功', 'ORDER_PAID', 2, '尊敬的${username}，您的订单${orderSn}已支付成功，金额${amount}元', '["username", "orderSn", "amount"]'),
('订单发货通知', 'ORDER_SHIPPED', 3, '您的订单${orderSn}已发货，快递公司：${expressCompany}，单号：${expressNo}', '["orderSn", "expressCompany", "expressNo"]'),
('优惠券到账', 'COUPON_RECEIVED', 4, '您获得了一张${couponName}优惠券，满${minAmount}元可用，有效期至${expireDate}', '["couponName", "minAmount", "expireDate"]');

-- ============================================
-- 索引说明：
-- 1. notification_message.idx_member_id: 支持查询用户的所有消息
-- 2. sms_send_record.uk_biz_id: 保证短信发送的幂等性
-- 3. email_send_record.uk_biz_id: 保证邮件发送的幂等性
-- 4. push_notification_record.uk_biz_id: 保证推送的幂等性
-- 5. message_template.uk_template_code: 保证模板代码唯一性
-- ============================================
