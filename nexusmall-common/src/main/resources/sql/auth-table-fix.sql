-- 修复权限表缺少 description 字段的问题
-- 执行时间：2026-03-24

-- 添加 description 字段（如果不存在）
ALTER TABLE `sys_permission` 
ADD COLUMN IF NOT EXISTS `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述' AFTER `method`;

-- 为现有权限数据添加描述
UPDATE `sys_permission` SET `description` = '系统管理相关功能' WHERE `permission_code` = 'system';
UPDATE `sys_permission` SET `description` = '用户管理相关功能' WHERE `permission_code` = 'system:user';
UPDATE `sys_permission` SET `description` = '查看用户列表' WHERE `permission_code` = 'system:user:list';
UPDATE `sys_permission` SET `description` = '创建新用户' WHERE `permission_code` = 'system:user:create';
UPDATE `sys_permission` SET `description` = '更新用户信息' WHERE `permission_code` = 'system:user:update';
UPDATE `sys_permission` SET `description` = '删除用户' WHERE `permission_code` = 'system:user:delete';
UPDATE `sys_permission` SET `description` = '角色管理相关功能' WHERE `permission_code` = 'system:role';
UPDATE `sys_permission` SET `description` = '查看角色列表' WHERE `permission_code` = 'system:role:list';
UPDATE `sys_permission` SET `description` = '权限管理相关功能' WHERE `permission_code` = 'system:permission';
UPDATE `sys_permission` SET `description` = '商品管理相关功能' WHERE `permission_code` = 'product';
UPDATE `sys_permission` SET `description` = '查看商品列表' WHERE `permission_code` = 'product:list';
UPDATE `sys_permission` SET `description` = '创建新商品' WHERE `permission_code` = 'product:create';
UPDATE `sys_permission` SET `description` = '订单管理相关功能' WHERE `permission_code` = 'order';
UPDATE `sys_permission` SET `description` = '查看订单列表' WHERE `permission_code` = 'order:list';
