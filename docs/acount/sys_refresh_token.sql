-- =====================================================
-- NexusMall Auth Service - Refresh Token Table
-- =====================================================
-- 说明：
-- 1. 用于存储 Refresh Token，支持主动撤销
-- 2. 记录设备信息，便于安全管理
-- 3. 配合 Redis 黑名单实现 Token 即时失效
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_refresh_token` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `token` TEXT NOT NULL COMMENT 'Refresh Token (JWT)',
    `jti` VARCHAR(128) NOT NULL COMMENT 'Token唯一标识(JTI)，用于黑名单',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `device_info` VARCHAR(256) DEFAULT NULL COMMENT '设备信息',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=无效，1=有效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_jti` (`jti`) COMMENT 'JTI唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_username` (`username`) COMMENT '用户名索引',
    KEY `idx_expire_time` (`expire_time`) COMMENT '过期时间索引（用于定时清理）',
    KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token表';

-- =====================================================
-- 索引说明：
-- 1. uk_jti: JTI唯一索引，防止重复Token，加速黑名单查询
-- 2. idx_user_id: 用户ID索引，快速查询用户的所有Token
-- 3. idx_username: 用户名索引，支持按用户名查询
-- 4. idx_expire_time: 过期时间索引，支持定时任务清理过期Token
-- 5. idx_status: 状态索引，快速筛选有效/无效Token
-- =====================================================

-- =====================================================
-- 数据清理策略：
-- 建议创建定时任务，每天清理过期的 Refresh Token
-- DELETE FROM sys_refresh_token WHERE expire_time < NOW() OR status = 0;
-- =====================================================
