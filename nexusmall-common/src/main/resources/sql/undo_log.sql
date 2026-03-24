-- Seata 分布式事务回滚日志表
-- 需要在每个参与分布式事务的数据库中创建此表

-- nexusmall_order 数据库
USE nexusmall_order;
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `branch_id` BIGINT(20) NOT NULL COMMENT '分支事务 ID',
    `xid` VARCHAR(100) NOT NULL COMMENT '全局事务 ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文信息',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT(11) NOT NULL COMMENT '日志状态：0-正常，1-已清理',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`branch_id`, `xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata 分布式事务回滚日志表';

-- nexusmall_product 数据库
USE nexusmall_product;
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `branch_id` BIGINT(20) NOT NULL COMMENT '分支事务 ID',
    `xid` VARCHAR(100) NOT NULL COMMENT '全局事务 ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文信息',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT(11) NOT NULL COMMENT '日志状态：0-正常，1-已清理',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`branch_id`, `xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata 分布式事务回滚日志表';

-- nexusmall_auth 数据库（如果需要）
USE nexusmall_auth;
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `branch_id` BIGINT(20) NOT NULL COMMENT '分支事务 ID',
    `xid` VARCHAR(100) NOT NULL COMMENT '全局事务 ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文信息',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT(11) NOT NULL COMMENT '日志状态：0-正常，1-已清理',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`branch_id`, `xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata 分布式事务回滚日志表';
