package com.nexusmall.common.database.base;

/**
 * 基础 Mapper 接口
 * <p>
 * 所有 Mapper 接口都应继承此接口，获得 MyBatis-Plus 提供的通用 CRUD 方法
 * </p>
 *
 * @param <T> 实体类型
 * @author shudl
 * @since 2026-04-09
 */
public interface BaseRepository<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
    // 继承 MyBatis-Plus 的 BaseMapper，自动获得以下方法：
    // - insert(T entity)
    // - deleteById(Serializable id)
    // - updateById(T entity)
    // - selectById(Serializable id)
    // - selectList(Wrapper<T> queryWrapper)
    // - selectPage(Page<T> page, Wrapper<T> queryWrapper)
    // ... 等更多方法
}
