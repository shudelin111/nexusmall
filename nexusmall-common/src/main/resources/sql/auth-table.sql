-- ====================================================
-- Auth 模块数据库初始化脚本
-- 数据库：nexusmall_auth
-- ====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `nexusmall_auth` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

USE `nexusmall_auth`;

-- 2. 创建 UNDO_LOG 表（Seata 分布式事务需要，预留）
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

-- 3. 创建用户表
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

-- 4. 创建角色表
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

-- 5. 创建权限表
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

-- 6. 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 7. 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色 ID',
    `permission_id` BIGINT(20) NOT NULL COMMENT '权限 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 8. 插入测试数据 - 用户（密码是 123456 的 BCrypt 加密）
-- BCrypt hash for '123456': $2a$10$7HB0WZdXfS9vVQkCqL5jMOyqHCpFzQlWwGMxPmHhTqJzKxPLmFJKq
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '管理员', '13800138000', 'admin@nexusmall.com', 1),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '张三', '13800138001', 'user1@test.com', 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJqp9jKxPLmFJKqGzA5hVxNQsL.', '李四', '13800138002', 'user2@test.com', 1);

-- 9. 插入测试数据 - 角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`) VALUES
('ROLE_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
('ROLE_USER', '普通用户', '普通用户，拥有基础权限', 1),
('ROLE_VIP', 'VIP 用户', 'VIP 用户，拥有高级权限', 1);

-- 10. 插入测试数据 - 权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource_type`, `parent_id`, `path`, `method`, `sort_order`, `status`) VALUES
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

-- 11. 插入测试数据 - 用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- user1 -> ROLE_USER
(3, 2); -- user2 -> ROLE_USER

-- 12. 插入测试数据 - 角色权限关联
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), -- ADMIN 拥有系统管理所有权限
(1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12), -- ADMIN 拥有商品管理所有权限
(1, 13), (1, 14), -- ADMIN 拥有订单管理所有权限
(2, 10), (2, 11), -- USER 只能查看商品列表
(2, 13), (2, 14); -- USER 只能查看订单列表
