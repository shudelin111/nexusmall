package com.nexusmall.promotion.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.promotion.domain.entity.CouponUserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户优惠券领取记录 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface CouponUserRecordMapper extends BaseMapper<CouponUserRecord> {

    /**
     * 统计用户已领取的某优惠券数量
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 领取数量
     */
    @Select("SELECT COUNT(*) FROM promotion_coupon_user_record WHERE user_id = #{userId} AND coupon_id = #{couponId} AND deleted = 0")
    Integer countByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
