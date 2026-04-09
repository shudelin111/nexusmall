package com.nexusmall.common.database.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * <p>
 * 所有业务实体都应继承此类，提供通用的审计字段和乐观锁支持
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充：插入和更新时）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 版本号（乐观锁）
     * <p>
     * 使用时需在数据库表中添加 version 字段，默认值为 0 或 1
     * </p>
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
