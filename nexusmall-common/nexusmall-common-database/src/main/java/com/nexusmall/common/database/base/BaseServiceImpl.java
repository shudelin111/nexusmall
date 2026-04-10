package com.nexusmall.common.database.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 基础 Service 实现类
 * <p>
 * 所有 Service 实现类都应继承此类，获得 MyBatis-Plus 提供的通用 CRUD 方法实现
 * </p>
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @author shudl
 * @since 2026-04-09
 */
public abstract class BaseServiceImpl<M extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, T> 
        extends ServiceImpl<M, T> implements BaseService<T> {
    // 继承 ServiceImpl，自动获得所有 CRUD 方法实现
}
