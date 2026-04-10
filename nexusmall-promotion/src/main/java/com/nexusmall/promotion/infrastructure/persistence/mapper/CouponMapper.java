package com.nexusmall.promotion.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.promotion.domain.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}
