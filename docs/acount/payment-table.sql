-- =====================================================
-- NexusMall Payment Service - 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 创建数据库
-- 2. 支付单管理（创建、查询、状态更新）
-- 3. 支付渠道配置（支付宝、微信、银联等）
-- 4. 退款管理（申请、审核、执行）
-- 5. 对账记录（与第三方支付平台对账）
-- 6. 支付流水日志（完整审计追踪）
-- =====================================================

-- ============================================
-- 0. 创建数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `nexusmall_payment` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_payment`;

-- ============================================
-- 1. 支付单表（核心表）
-- ============================================
CREATE TABLE IF NOT EXISTS `pay_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号（业务唯一标识）',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（关联订单服务）',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `member_id` BIGINT(20) DEFAULT NULL COMMENT '会员ID',
    
    -- 支付金额信息
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `refund_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '已退款金额',
    
    -- 支付渠道信息
    `channel_code` VARCHAR(32) NOT NULL COMMENT '支付渠道编码：ALIPAY/WECHAT/UNIONPAY',
    `channel_name` VARCHAR(64) DEFAULT NULL COMMENT '支付渠道名称',
    `trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号（支付宝/微信返回）',
    
    -- 支付状态
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '支付状态：0=待支付，1=支付中，2=支付成功，3=支付失败，4=已关闭，5=已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
    `expire_time` DATETIME NOT NULL COMMENT '支付过期时间',
    
    -- 回调信息
    `callback_content` TEXT COMMENT '第三方支付回调原始数据',
    `callback_time` DATETIME DEFAULT NULL COMMENT '回调时间',
    
    -- 备注信息
    `subject` VARCHAR(256) DEFAULT NULL COMMENT '商品描述',
    `body` VARCHAR(512) DEFAULT NULL COMMENT '商品详情',
    `client_ip` VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '用户代理',
    
    -- 系统字段
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`) COMMENT '支付单号唯一索引',
    KEY `idx_order_no` (`order_no`) COMMENT '订单号索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_trade_no` (`trade_no`) COMMENT '第三方交易号索引',
    KEY `idx_status` (`status`) COMMENT '支付状态索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

-- ============================================
-- 2. 支付渠道配置表
-- ============================================
CREATE TABLE IF NOT EXISTS `pay_channel_config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '渠道编码：ALIPAY/WECHAT/UNIONPAY',
    `channel_name` VARCHAR(64) NOT NULL COMMENT '渠道名称',
    `channel_type` VARCHAR(32) NOT NULL COMMENT '渠道类型：THIRD_PARTY(第三方)/BANK(银行)',
    
    -- 配置信息（加密存储）
    `app_id` VARCHAR(128) NOT NULL COMMENT '应用ID',
    `merchant_id` VARCHAR(128) NOT NULL COMMENT '商户号',
    `private_key` TEXT COMMENT '私钥（加密存储）',
    `public_key` TEXT COMMENT '公钥',
    `cert_path` VARCHAR(256) DEFAULT NULL COMMENT '证书路径',
    
    -- 回调配置
    `notify_url` VARCHAR(256) NOT NULL COMMENT '异步通知地址',
    `return_url` VARCHAR(256) DEFAULT NULL COMMENT '同步返回地址',
    
    -- 费率配置
    `fee_rate` DECIMAL(5,4) DEFAULT 0.0060 COMMENT '手续费率（默认0.6%）',
    `min_fee` DECIMAL(10,2) DEFAULT 0.01 COMMENT '最低手续费',
    
    -- 状态控制
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
    
    -- 系统字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_code` (`channel_code`) COMMENT '渠道编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道配置表';

-- 初始化默认支付渠道配置
INSERT INTO `pay_channel_config` (`channel_code`, `channel_name`, `channel_type`, `app_id`, `merchant_id`, `notify_url`, `fee_rate`) VALUES
('ALIPAY', '支付宝', 'THIRD_PARTY', 'your_alipay_app_id', 'your_merchant_id', 'https://api.nexusmall.com/pay/callback/alipay', 0.0060),
('WECHAT', '微信支付', 'THIRD_PARTY', 'your_wechat_app_id', 'your_merchant_id', 'https://api.nexusmall.com/pay/callback/wechat', 0.0060),
('UNIONPAY', '银联支付', 'BANK', 'your_unionpay_app_id', 'your_merchant_id', 'https://api.nexusmall.com/pay/callback/unionpay', 0.0050);

-- ============================================
-- 3. 退款申请表
-- ============================================
CREATE TABLE IF NOT EXISTS `pay_refund` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号（业务唯一标识）',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    
    -- 退款金额
    `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    `refund_reason` VARCHAR(512) DEFAULT NULL COMMENT '退款原因',
    
    -- 退款状态
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '退款状态：0=待审核，1=审核通过，2=审核拒绝，3=退款中，4=退款成功，5=退款失败',
    `audit_user_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_remark` VARCHAR(512) DEFAULT NULL COMMENT '审核备注',
    
    -- 第三方退款信息
    `channel_refund_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方退款单号',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    `callback_content` TEXT COMMENT '第三方退款回调数据',
    
    -- 系统字段
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`) COMMENT '退款单号唯一索引',
    KEY `idx_payment_no` (`payment_no`) COMMENT '支付单号索引',
    KEY `idx_order_no` (`order_no`) COMMENT '订单号索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_status` (`status`) COMMENT '退款状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

-- ============================================
-- 4. 对账记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `pay_reconciliation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reconciliation_no` VARCHAR(64) NOT NULL COMMENT '对账单号',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
    `bill_date` DATE NOT NULL COMMENT '账单日期',
    `bill_type` VARCHAR(32) NOT NULL COMMENT '账单类型：DAILY(日账单)/MONTHLY(月账单)',
    
    -- 对账结果
    `total_count` INT(11) NOT NULL DEFAULT 0 COMMENT '总笔数',
    `success_count` INT(11) NOT NULL DEFAULT 0 COMMENT '成功笔数',
    `fail_count` INT(11) NOT NULL DEFAULT 0 COMMENT '失败笔数',
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `success_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '成功金额',
    `refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    
    -- 差异信息
    `diff_count` INT(11) NOT NULL DEFAULT 0 COMMENT '差异笔数',
    `diff_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '差异金额',
    `diff_detail` TEXT COMMENT '差异详情（JSON格式）',
    
    -- 对账状态
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '对账状态：0=待对账，1=对账中，2=对账成功，3=对账失败，4=存在差异',
    `reconciliation_time` DATETIME DEFAULT NULL COMMENT '对账完成时间',
    
    -- 文件信息
    `bill_file_url` VARCHAR(512) DEFAULT NULL COMMENT '账单文件URL',
    `bill_file_hash` VARCHAR(64) DEFAULT NULL COMMENT '账单文件MD5',
    
    -- 系统字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reconciliation_no` (`reconciliation_no`) COMMENT '对账单号唯一索引',
    UNIQUE KEY `uk_channel_bill_date` (`channel_code`, `bill_date`, `bill_type`) COMMENT '渠道+日期+类型唯一索引',
    KEY `idx_bill_date` (`bill_date`) COMMENT '账单日期索引',
    KEY `idx_status` (`status`) COMMENT '对账状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账记录表';

-- ============================================
-- 5. 支付流水日志表（审计追踪）
-- ============================================
CREATE TABLE IF NOT EXISTS `pay_transaction_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `transaction_type` VARCHAR(32) NOT NULL COMMENT '交易类型：CREATE(创建)/PAY(支付)/REFUND(退款)/CLOSE(关闭)/NOTIFY(回调)',
    
    -- 请求信息
    `request_data` TEXT COMMENT '请求数据（JSON格式）',
    `response_data` TEXT COMMENT '响应数据（JSON格式）',
    `channel_response_code` VARCHAR(64) DEFAULT NULL COMMENT '渠道响应码',
    `channel_response_msg` VARCHAR(512) DEFAULT NULL COMMENT '渠道响应消息',
    
    -- 状态信息
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态：0=失败，1=成功',
    `error_message` VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
    
    -- 追踪信息
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
    `operator_type` VARCHAR(32) DEFAULT 'SYSTEM' COMMENT '操作人类型：USER(用户)/ADMIN(管理员)/SYSTEM(系统)',
    
    -- 系统字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (`id`),
    KEY `idx_payment_no` (`payment_no`) COMMENT '支付单号索引',
    KEY `idx_trace_id` (`trace_id`) COMMENT '链路追踪ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水日志表';

-- ============================================
-- 6. 支付分片表（可选，用于大数据量场景）
-- ============================================
-- 说明：当支付单数据量超过千万级时，可以考虑按 user_id 或 create_time 分片
-- 此处仅提供设计思路，实际使用时根据业务量决定是否启用

-- CREATE TABLE IF NOT EXISTS `pay_order_0` LIKE `pay_order`;
-- CREATE TABLE IF NOT EXISTS `pay_order_1` LIKE `pay_order`;
-- CREATE TABLE IF NOT EXISTS `pay_order_2` LIKE `pay_order`;
-- CREATE TABLE IF NOT EXISTS `pay_order_3` LIKE `pay_order`;

-- ============================================
-- 索引说明：
-- 1. pay_order.uk_payment_no: 保证支付单号全局唯一
-- 2. pay_order.idx_order_no: 支持按订单号查询支付单
-- 3. pay_order.idx_user_id: 支持查询用户的支付记录
-- 4. pay_order.idx_trade_no: 支持按第三方交易号查询
-- 5. pay_order.idx_status: 支持按状态筛选（如查询待支付订单）
-- 6. pay_refund.uk_refund_no: 保证退款单号全局唯一
-- 7. pay_refund.idx_payment_no: 支持查询支付单的退款记录
-- 8. pay_reconciliation.uk_channel_bill_date: 保证同一渠道同一天只有一份账单
-- 9. pay_transaction_log.idx_payment_no: 支持查询支付单的完整流水
-- ============================================

-- ============================================
-- 性能优化建议：
-- 1. 定期归档历史数据（如6个月前的支付单）
-- 2. 对大文本字段（callback_content）考虑使用压缩存储
-- 3. 监控慢查询，必要时添加覆盖索引
-- 4. 考虑使用分区表按月存储支付单
-- ============================================
