package com.nexusmall.cart.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.cart.domain.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车项 Mapper
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
