-- =====================================================
-- NexusMall 项目完整数据库初始化脚本
-- =====================================================
-- 作者：NexusMall Team
-- 版本：1.0.0
-- 日期：2026-03-26
-- 说明：此脚本包含所有服务模块的数据库和表结构
-- =====================================================

-- =====================================================
-- 第一部分：Nacos 配置中心数据库（可选）
-- =====================================================
-- 如果需要使用 Nacos 持久化配置，请执行以下脚本
-- 如果 Nacos 使用 Derby 嵌入式数据库，可以跳过此部分
-- =====================================================

CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE nacos_config;

CREATE TABLE `config_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(255) DEFAULT NULL,
    `content` longtext NOT NULL,
    `md5` varchar(32) DEFAULT NULL,
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user` text,
    `src_ip` varchar(50) DEFAULT NULL,
    `app_name` varchar(128) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    `c_desc` varchar(256) DEFAULT NULL,
    `c_use` varchar(64) DEFAULT NULL,
    `effect` varchar(64) DEFAULT NULL,
    `type` varchar(64) DEFAULT NULL,
    `c_schema` text,
    `encrypted_data_key` text NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `config_info_aggr` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(255) NOT NULL,
    `datum_id` varchar(255) NOT NULL,
    `content` longtext NOT NULL,
    `gmt_modified` datetime NOT NULL,
    `app_name` varchar(128) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `config_info_beta` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(255) NOT NULL,
    `app_name` varchar(128) DEFAULT NULL,
    `content` longtext NOT NULL,
    `beta_ips` varchar(1024) DEFAULT NULL,
    `md5` varchar(32) DEFAULT NULL,
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user` text,
    `src_ip` varchar(50) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    `encrypted_data_key` text NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `config_info_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(255) NOT NULL,
    `tag_id` varchar(128) NOT NULL,
    `app_name` varchar(128) DEFAULT NULL,
    `content` longtext NOT NULL,
    `md5` varchar(32) DEFAULT NULL,
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user` text,
    `src_ip` varchar(50) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    `encrypted_data_key` text NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `config_tags_relation` (
    `id` bigint(20) NOT NULL,
    `tag_name` varchar(128) NOT NULL,
    `tag_type` varchar(64) DEFAULT NULL,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(128) NOT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    `nid` bigint(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`nid`),
    UNIQUE KEY `uk_configtagrel_configid_tenant` (`id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `group_capacity` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `group_id` varchar(128) NOT NULL DEFAULT '',
    `quota` int(10) unsigned NOT NULL DEFAULT '0',
    `usage` int(10) unsigned NOT NULL DEFAULT '0',
    `max_size` int(10) unsigned NOT NULL DEFAULT '0',
    `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0',
    `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0',
    `max_history_count` int(10) unsigned NOT NULL DEFAULT '0',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `his_config_info` (
    `id` bigint(64) unsigned NOT NULL,
    `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(128) NOT NULL,
    `app_name` varchar(128) DEFAULT NULL,
    `content` longtext NOT NULL,
    `md5` varchar(32) DEFAULT NULL,
    `gmt_create` datetime NOT NULL DEFAULT '2010-05-05 00:00:00',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user` text,
    `src_ip` varchar(50) DEFAULT NULL,
    `op_type` char(10) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '',
    `encrypted_data_key` text NOT NULL,
    PRIMARY KEY (`nid`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `tenant_capacity` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `tenant_id` varchar(128) NOT NULL DEFAULT '',
    `quota` int(10) unsigned NOT NULL DEFAULT '0',
    `usage` int(10) unsigned NOT NULL DEFAULT '0',
    `max_size` int(10) unsigned NOT NULL DEFAULT '0',
    `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0',
    `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0',
    `max_history_count` int(10) unsigned NOT NULL DEFAULT '0',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `tenant_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `kp` varchar(128) NOT NULL,
    `tenant_id` varchar(128) default '',
    `tenant_name` varchar(128) default '',
    `tenant_desc` varchar(256) DEFAULT NULL,
    `create_source` varchar(32) DEFAULT NULL,
    `gmt_create` bigint(20) NOT NULL,
    `gmt_modified` bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `users` (
    `username` varchar(50) NOT NULL PRIMARY KEY,
    `password` varchar(500) NOT NULL,
    `enabled` boolean NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `roles` (
    `username` varchar(50) NOT NULL,
    `role` varchar(50) NOT NULL,
    UNIQUE INDEX `idx_user_role` (`username`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `permissions` (
    `role` varchar(50) NOT NULL,
    `resource` varchar(255) NOT NULL,
    `action` varchar(8) NOT NULL,
    UNIQUE INDEX `uk_role_permission` (`role`,`resource`,`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认管理员账号：nacos/nacos
INSERT INTO nacos_config.users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);
INSERT INTO nacos_config.roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');

-- =====================================================
-- 第二部分：认证服务数据库 (nexusmall_auth)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_auth` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_auth`;

-- Seata 分布式事务回滚日志表
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

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `deleted` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `deleted` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限 ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `resource_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '资源类型：1-菜单，2-按钮，3-接口',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父权限 ID',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '资源路径',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `deleted` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_resource_type` (`resource_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色 ID',
    `permission_id` BIGINT(20) NOT NULL COMMENT '权限 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 测试数据 - 用户（密码是 123456 的 BCrypt 加密）
INSERT INTO nexusmall_auth.sys_user (`username`, `password`, `nickname`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '管理员', '13800138000', 'admin@nexusmall.com', 1),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '张三', '13800138001', 'user1@test.com', 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '李四', '13800138002', 'user2@test.com', 1);

-- 测试数据 - 角色
INSERT INTO nexusmall_auth.sys_role (`role_code`, `role_name`, `description`, `status`) VALUES
('ROLE_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
('ROLE_USER', '普通用户', '普通用户，拥有基础权限', 1),
('ROLE_VIP', 'VIP 用户', 'VIP 用户，拥有高级权限', 1);

-- 测试数据 - 权限
INSERT INTO nexusmall_auth.sys_permission (`permission_code`, `permission_name`, `resource_type`, `parent_id`, `path`, `method`, `sort_order`, `status`) VALUES
('system', '系统管理', 1, 0, '/system', NULL, 1, 1),
('system:user', '用户管理', 1, 1, '/system/user', NULL, 1, 1),
('system:user:list', '用户列表', 2, 2, '/auth/user/list', 'GET', 1, 1),
('system:user:create', '创建用户', 2, 2, '/auth/user/create', 'POST', 2, 1),
('system:user:update', '更新用户', 2, 2, '/auth/user/update', 'PUT', 3, 1),
('system:user:delete', '删除用户', 2, 2, '/auth/user/delete', 'DELETE', 4, 1),
('system:role', '角色管理', 1, 1, '/system/role', NULL, 2, 1),
('system:role:list', '角色列表', 2, 7, '/auth/role/list', 'GET', 1, 1),
('system:permission', '权限管理', 1, 1, '/system/permission', NULL, 3, 1),
('product', '商品管理', 1, 0, '/product', NULL, 2, 1),
('product:list', '商品列表', 2, 10, '/product/list', 'GET', 1, 1),
('product:create', '创建商品', 2, 10, '/product/create', 'POST', 2, 1),
('order', '订单管理', 1, 0, '/order', NULL, 3, 1),
('order:list', '订单列表', 2, 13, '/order/list', 'GET', 1, 1);

-- 测试数据 - 用户角色关联
INSERT INTO nexusmall_auth.sys_user_role (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 2);

-- 测试数据 - 角色权限关联
INSERT INTO nexusmall_auth.sys_role_permission (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12),
(1, 13), (1, 14),
(2, 10), (2, 11),
(2, 13), (2, 14);

-- =====================================================
-- 第三部分：商品服务数据库 (nexusmall_product)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_product` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_product`;

-- Seata 分布式事务回滚日志表
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

-- 商品表
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

-- 商品分类表
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

-- 商品品牌表
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

-- 测试数据 - 品牌
INSERT INTO nexusmall_product.brand (`name`, `logo`, `description`, `first_letter`, `sort_order`, `status`) VALUES
('华为', '/images/brands/huawei.png', '华为技术有限公司', 'H', 1, 1),
('小米', '/images/brands/xiaomi.png', '小米科技有限责任公司', 'X', 2, 1),
('苹果', '/images/brands/apple.png', '苹果公司', 'P', 3, 1);

-- 测试数据 - 分类
INSERT INTO nexusmall_product.category (`name`, `parent_id`, `level`, `sort_order`, `status`) VALUES
('手机数码', 0, 1, 1, 1),
('电脑办公', 0, 1, 2, 1),
('家用电器', 0, 1, 3, 1),
('手机通讯', 1, 2, 1, 1),
('手机配件', 1, 2, 2, 1),
('电脑整机', 2, 2, 1, 1),
('电脑配件', 2, 2, 2, 1);

-- 测试数据 - 商品
INSERT INTO nexusmall_product.product (`sku_name`, `price`, `stock`, `category_id`, `category_name`, `brand_id`, `brand_name`, `description`, `status`) VALUES
('华为 Mate 60 Pro 12GB+512GB', 6988.00, 1000, 1, '手机数码', 1, '华为', '华为旗舰手机，搭载麒麟 9000S 处理器', 1),
('小米 14 Pro 16GB+1TB', 4999.00, 2000, 1, '手机数码', 2, '小米', '小米旗舰手机，搭载骁龙 8 Gen 3 处理器', 1),
('iPhone 15 Pro Max 256GB', 9999.00, 500, 1, '手机数码', 3, '苹果', '苹果旗舰手机，A17 Pro 芯片', 1);

-- =====================================================
-- 第四部分：订单服务数据库 (nexusmall_order)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_order` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_order`;

-- Seata 分布式事务回滚日志表
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

-- 订单表
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

-- 订单项表
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

-- 测试数据 - 订单
INSERT INTO nexusmall_order.`order` (`order_sn`, `member_id`, `receiver_name`, `receiver_phone`, `receiver_address`, `total_amount`, `pay_amount`, `freight_amount`, `promotion_amount`, `status`, `payment_type`, `remark`) VALUES
('ORD-2026032400001', 1, '张三', '13800138001', '北京市朝阳区 xx 街道 xx 号', 6988.00, 6988.00, 0.00, 0.00, 1, 1, '请尽快发货'),
('ORD-2026032400002', 2, '李四', '13800138002', '上海市浦东新区 xx 路 xx 号', 4999.00, 4999.00, 10.00, 0.00, 2, 2, '周末送货'),
('ORD-2026032400003', 3, '王五', '13800138003', '广州市天河区 xx 大厦 xx 室', 14998.00, 14998.00, 0.00, 0.00, 3, 1, '企业采购');

-- 测试数据 - 订单项
INSERT INTO nexusmall_order.order_item (`order_id`, `order_sn`, `sku_id`, `sku_name`, `sku_price`, `quantity`, `subtotal`) VALUES
(1, 'ORD-2026032400001', 1, '华为 Mate 60 Pro 12GB+512GB', 6988.00, 1, 6988.00),
(2, 'ORD-2026032400002', 2, '小米 14 Pro 16GB+1TB', 4999.00, 1, 4999.00),
(3, 'ORD-2026032400003', 1, '华为 Mate 60 Pro 12GB+512GB', 6988.00, 2, 13976.00),
(3, 'ORD-2026032400003', 3, 'iPhone 15 Pro Max 256GB', 9999.00, 1, 9999.00);

-- =====================================================
-- 第五部分：行为服务数据库 (nexusmall_behavior)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_behavior` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_behavior`;

-- Seata 分布式事务回滚日志表
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

-- 用户行为日志表
CREATE TABLE IF NOT EXISTS `user_behavior_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `user_name` VARCHAR(64) DEFAULT NULL COMMENT '用户名（冗余字段，便于查询）',
    `behavior_type` VARCHAR(32) NOT NULL COMMENT '行为类型（如：PLACE_ORDER-下单）',
    `behavior_desc` VARCHAR(128) DEFAULT NULL COMMENT '行为描述',
    `object_id` BIGINT(20) DEFAULT NULL COMMENT '业务对象 ID（如商品 ID、订单 ID）',
    `object_type` VARCHAR(32) DEFAULT NULL COMMENT '业务对象类型（如：product_id、order_id）',
    `object_name` VARCHAR(128) DEFAULT NULL COMMENT '业务对象名称（冗余字段）',
    `extra_data` TEXT DEFAULT NULL COMMENT '额外信息（JSON 格式）',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
    `device_type` VARCHAR(32) DEFAULT NULL COMMENT '设备类型（mobile/pc/app）',
    `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
    `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器',
    `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `occur_time` DATETIME NOT NULL COMMENT '行为发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '按用户 ID 查询',
    KEY `idx_behavior_type` (`behavior_type`) COMMENT '按行为类型统计',
    KEY `idx_object` (`object_type`, `object_id`) COMMENT '按业务对象查询',
    KEY `idx_occur_time` (`occur_time`) COMMENT '按时间范围查询',
    KEY `idx_create_time` (`create_time`) COMMENT '按创建时间查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 测试数据 - 用户行为日志
INSERT INTO nexusmall_behavior.user_behavior_log 
(`user_id`, `behavior_type`, `behavior_desc`, `object_id`, `object_type`, `extra_data`, `ip_address`, `occur_time`) 
VALUES 
(1001, 'PLACE_ORDER', '下单购买', 10001, 'order_id', '{"amount": 299.00}', '192.168.1.100', NOW()),
(1002, 'PLACE_ORDER', '下单购买', 10002, 'order_id', '{"amount": 599.00}', '192.168.1.101', NOW());

-- =====================================================
-- 第六部分：会员服务数据库 (nexusmall_member)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_member` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_member`;

-- Seata 分布式事务回滚日志表
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

-- 会员表
CREATE TABLE IF NOT EXISTS `member` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会员 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID（关联 sys_user）',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
    `gender` TINYINT(4) DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `integration` INT(11) NOT NULL DEFAULT 0 COMMENT '积分',
    `growth` INT(11) NOT NULL DEFAULT 0 COMMENT '成长值',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_integration` (`integration`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='会员信息表';

-- 会员收货地址表
CREATE TABLE IF NOT EXISTS `member_receive_address` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址 ID',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(100) NOT NULL COMMENT '省份',
    `city` VARCHAR(100) NOT NULL COMMENT '城市',
    `district` VARCHAR(100) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(500) NOT NULL COMMENT '详细地址',
    `default_status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='会员收货地址表';

-- 测试数据 - 会员
INSERT INTO nexusmall_member.member (`user_id`, `nickname`, `phone`, `integration`, `growth`, `status`) VALUES
(1, '管理员', '13800138000', 1000, 500, 1),
(2, '张三', '13800138001', 500, 200, 1),
(3, '李四', '13800138002', 300, 100, 1);

-- 测试数据 - 收货地址
INSERT INTO nexusmall_member.member_receive_address (`member_id`, `name`, `phone`, `province`, `city`, `district`, `detail_address`, `default_status`) VALUES
(1, '管理员', '13800138000', '北京市', '北京市', '朝阳区', 'xx街道 xx号', 1),
(2, '张三', '13800138001', '上海市', '上海市', '浦东新区', 'xx路 xx号', 1),
(3, '李四', '13800138002', '广东省', '广州市', '天河区', 'xx大厦 xx室', 1);

-- =====================================================
-- 第七部分：购物车服务数据库 (nexusmall_cart)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_cart` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nexusmall_cart`;

-- Seata 分布式事务回滚日志表
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

-- 购物车项表
CREATE TABLE IF NOT EXISTS `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sku_id` BIGINT NOT NULL COMMENT '商品SKU ID',
  `spu_id` BIGINT NOT NULL COMMENT '商品SPU ID',
  
  -- 商品快照字段(关键！)
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称快照(加入时)',
  `product_image` VARCHAR(500) COMMENT '商品主图快照',
  `snapshot_price` DECIMAL(10,2) NOT NULL COMMENT '加入时价格快照(永不改变)',
  `snapshot_attrs` JSON COMMENT '商品属性快照JSON(color/storage等)',
  `snapshot_version` INT NOT NULL DEFAULT 0 COMMENT '商品快照版本号',
  `snapshot_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照创建时间',
  
  -- 购物车业务字段
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中: 0-否 1-是',
  
  -- 审计字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`) COMMENT '用户+SKU唯一索引(幂等性保证)',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引(快速查询购物车)',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引(定时清理)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- 测试数据 - 购物车项
INSERT INTO nexusmall_cart.cart_item (`user_id`, `sku_id`, `spu_id`, `product_name`, `product_image`, `snapshot_price`, `snapshot_attrs`, `quantity`, `selected`) 
VALUES 
(1, 1001, 101, 'iPhone 15 Pro', 'https://example.com/iphone15.jpg', 8999.00, '{"color": "深空灰", "storage": "128GB"}', 2, 1),
(1, 1002, 102, 'MacBook Air M3', 'https://example.com/macbook.jpg', 7999.00, '{"color": "银色", "memory": "16GB"}', 1, 1);

-- =====================================================
-- 第八部分：支付服务数据库 (nexusmall_payment)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_payment` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_payment`;

-- Seata 分布式事务回滚日志表
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

-- 支付单表
CREATE TABLE IF NOT EXISTS `pay_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
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
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

-- 退款单表
CREATE TABLE IF NOT EXISTS `refund_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `member_id` BIGINT(20) DEFAULT NULL COMMENT '会员ID',
    
    -- 退款金额
    `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
    
    -- 退款状态
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '退款状态：0=申请中，1=审核中，2=审核通过，3=审核拒绝，4=退款中，5=退款成功，6=退款失败',
    
    -- 审核信息
    `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(100) DEFAULT NULL COMMENT '审核人姓名',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    
    -- 退款执行信息
    `refund_trade_no` VARCHAR(128) DEFAULT NULL COMMENT '退款交易号',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='退款单表';

-- 测试数据 - 支付单
INSERT INTO nexusmall_payment.pay_order (`payment_no`, `order_no`, `member_id`, `total_amount`, `pay_amount`, `channel_code`, `channel_name`, `status`, `expire_time`, `subject`) VALUES
('PAY2026040600001', 'ORD-2026032400001', 1, 6988.00, 6988.00, 'ALIPAY', '支付宝', 2, DATE_ADD(NOW(), INTERVAL 30 MINUTE), '华为 Mate 60 Pro'),
('PAY2026040600002', 'ORD-2026032400002', 2, 4999.00, 4999.00, 'WECHAT', '微信支付', 2, DATE_ADD(NOW(), INTERVAL 30 MINUTE), '小米 14 Pro');

-- =====================================================
-- 第九部分：第三方服务数据库 (nexusmall_third_party) - 预留
-- =====================================================
-- 注意：当前第三方服务模块没有特定的表结构需求
-- 如果后续需要存储 OSS 配置、短信日志等，可以在此添加
-- =====================================================

CREATE DATABASE IF NOT EXISTS `nexusmall_third_party` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_third_party`;

-- Seata 分布式事务回滚日志表（预留）
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

-- =====================================================
-- 数据库初始化完成
-- =====================================================
