package com.nexusmall.promotion.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.promotion.domain.entity.Coupon;

/**
 * 优惠?Service 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 领取优惠?
     *
     * @param couponId 优惠券ID
     * @param userId   用户ID
     * @return 是否成功
     */
    boolean receiveCoupon(Long couponId, Long userId);

    /**
     * 查询用户可领取的优惠券列?
     *
     * @param userId 用户ID
     * @return 优惠券列?
     */
    java.util.List<Coupon> listAvailableCoupons(Long userId);
}
