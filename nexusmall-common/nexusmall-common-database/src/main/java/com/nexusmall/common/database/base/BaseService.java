package com.nexusmall.common.database.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 基础 Service 接口
 * <p>
 * 所有 Service 接口都应继承此接口，获得 MyBatis-Plus 提供的通用 CRUD 方法
 * </p>
 *
 * @param <T> 实体类型
 * @author shudl
 * @since 2026-04-09
 */
public interface BaseService<T> extends IService<T> {
    
    /**
     * 分页查询（简化版）
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    default IPage<T> page(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }

    /**
     * 根据条件分页查询
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页大小
     * @param wrapper  查询条件
     * @return 分页结果
     */
    default IPage<T> page(int pageNum, int pageSize, QueryWrapper<T> wrapper) {
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 查询列表
     *
     * @param wrapper 查询条件
     * @return 列表结果
     */
    default List<T> list(QueryWrapper<T> wrapper) {
        return this.list(wrapper);
    }
}
