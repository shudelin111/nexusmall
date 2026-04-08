-- =============================================
-- NexusMall Promotion Module Database Schema
-- 营销模块数据库表结构
-- =============================================

-- 1. 优惠券主表
CREATE TABLE IF NOT EXISTS `promotion_coupon` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `code` VARCHAR(50) NOT NULL COMMENT '优惠券编码（唯一标识）',
  `type` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '优惠类型：1-满减 2-折扣 3-立减',
  `value` DECIMAL(10,2) NOT NULL COMMENT '面值/折扣率',
  `min_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '最低消费金额（满减门槛）',
  `max_discount` DECIMAL(10,2) DEFAULT NULL COMMENT '最高优惠金额（封顶）',
  `scope` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '使用范围：0-全场 1-指定分类 2-指定商品',
  `scope_data` TEXT COMMENT '适用范围JSON（分类ID列表或商品ID列表）',
  `total_stock` INT(11) NOT NULL DEFAULT '0' COMMENT '总库存',
  `received_count` INT(11) NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `per_limit` INT(11) NOT NULL DEFAULT '0' COMMENT '每人限领数量（0表示不限制）',
  `valid_type` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '有效期类型：1-固定日期 2-领取后N天',
  `valid_start` DATETIME DEFAULT NULL COMMENT '有效期开始时间',
  `valid_end` DATETIME DEFAULT NULL COMMENT '有效期结束时间',
  `valid_days` INT(11) DEFAULT NULL COMMENT '领取后有效天数',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未开始 1-进行中 2-已结束 3-已下架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_valid_time` (`valid_start`, `valid_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券主表';

-- 2. 用户优惠券领取记录表
CREATE TABLE IF NOT EXISTS `promotion_coupon_receive` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '领取记录ID',
  `coupon_id` BIGINT(20) NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未使用 1-已使用 2-已过期',
  `order_id` BIGINT(20) DEFAULT NULL COMMENT '使用订单ID',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券领取记录表';

-- 3. 秒杀活动表
CREATE TABLE IF NOT EXISTS `promotion_flash_sale` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未开始 1-进行中 2-已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`, `end_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 4. 秒杀商品表
CREATE TABLE IF NOT EXISTS `promotion_flash_sale_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `flash_sale_id` BIGINT(20) NOT NULL COMMENT '活动ID',
  `sku_id` BIGINT(20) NOT NULL COMMENT 'SKU ID',
  `product_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称（冗余字段）',
  `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余字段）',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `flash_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
  `stock` INT(11) NOT NULL DEFAULT '0' COMMENT '秒杀库存',
  `sold_count` INT(11) NOT NULL DEFAULT '0' COMMENT '已售数量',
  `limit_per_user` INT(11) NOT NULL DEFAULT '1' COMMENT '每人限购数量',
  `sort_order` INT(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `version` INT(11) NOT NULL DEFAULT '0' COMMENT '版本号（乐观锁）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_sku` (`flash_sale_id`, `sku_id`),
  KEY `idx_flash_sale_id` (`flash_sale_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 5. 满减活动表
CREATE TABLE IF NOT EXISTS `promotion_discount` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `type` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '类型：1-满减 2-满折',
  `rules` JSON NOT NULL COMMENT '规则JSON [{"minAmount":100,"discount":20}]',
  `scope` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '使用范围：0-全场 1-指定分类 2-指定商品',
  `scope_data` TEXT COMMENT '适用范围JSON',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未开始 1-进行中 2-已结束',
  `priority` INT(11) NOT NULL DEFAULT '0' COMMENT '优先级（数字越大优先级越高）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满减活动表';

-- 6. 消息通知表
CREATE TABLE IF NOT EXISTS `promotion_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（0表示全员通知）',
  `type` TINYINT(4) NOT NULL COMMENT '通知类型：1-优惠券到期 2-秒杀开始 3-活动提醒 4-系统公告',
  `channel` TINYINT(4) NOT NULL COMMENT '通知渠道：1-站内信 2-短信 3-邮件 4-APP推送',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
  `extra_data` TEXT COMMENT '扩展数据JSON',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未发送 1-已发送 2-发送失败',
  `is_read` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_type` (`type`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 7. 营销数据统计表（按天聚合）
CREATE TABLE IF NOT EXISTS `promotion_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_type` TINYINT(4) NOT NULL COMMENT '统计类型：1-优惠券 2-秒杀 3-满减 4-综合',
  `activity_id` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '活动ID（0表示汇总）',
  `impression_count` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '曝光次数',
  `click_count` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '点击次数',
  `receive_count` INT(11) NOT NULL DEFAULT '0' COMMENT '领取数量（优惠券）',
  `use_count` INT(11) NOT NULL DEFAULT '0' COMMENT '使用数量',
  `order_count` INT(11) NOT NULL DEFAULT '0' COMMENT '下单数量',
  `gmv` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '成交金额',
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额',
  `click_rate` DECIMAL(10,4) DEFAULT '0.0000' COMMENT '转化率（点击/曝光）',
  `receive_rate` DECIMAL(10,4) DEFAULT '0.0000' COMMENT '领取率（领取/点击）',
  `use_rate` DECIMAL(10,4) DEFAULT '0.0000' COMMENT '使用率（使用/领取）',
  `roi` DECIMAL(10,4) DEFAULT '0.0000' COMMENT 'ROI（成交金额/优惠金额）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type_activity` (`stat_date`, `stat_type`, `activity_id`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销数据统计表';
