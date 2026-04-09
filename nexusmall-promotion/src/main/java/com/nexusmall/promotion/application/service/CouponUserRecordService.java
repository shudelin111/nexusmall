package com.nexusmall.promotion.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.promotion.domain.entity.CouponUserRecord;
import com.nexusmall.promotion.interfaces.dto.UserCouponVO;

import java.util.List;

/**
 * 用户优惠券领取记�?Service 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface CouponUserRecordService extends IService<CouponUserRecord> {

    /**
     * 查询用户的优惠券列表（按状态分类）
     *
     * @param userId    用户ID
     * @param useStatus 使用状态（null表示查询全部�?
     * @return 优惠券列�?
     */
    List<UserCouponVO> listUserCoupons(Long userId, Integer useStatus);

    /**
     * 核销优惠券（下单时使用）
     *
     * @param recordId 领取记录ID
     * @param orderId  订单ID
     * @return 是否成功
     */
    boolean useCoupon(Long recordId, Long orderId);

    /**
     * 锁定优惠券（下单未支付时�?
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    boolean lockCoupon(Long recordId);

    /**
     * 释放优惠券（取消订单或支付失败时�?
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    boolean releaseCoupon(Long recordId);

    /**
     * 回退优惠券（退款时�?
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    boolean refundCoupon(Long recordId);

    /**
     * 统计用户已领取的某优惠券数量
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 领取数量
     */
    Integer countByUserIdAndCouponId(Long userId, Long couponId);
}
