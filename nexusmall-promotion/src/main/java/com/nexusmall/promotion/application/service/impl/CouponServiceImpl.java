package com.nexusmall.promotion.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.promotion.application.service.CouponService;
import com.nexusmall.promotion.domain.entity.Coupon;
import com.nexusmall.promotion.infrastructure.persistence.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Long couponId, Long userId) {
        log.info("领取优惠�? couponId={}, userId={}", couponId, userId);
        
        // 1. 查询优惠券信�?
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            log.warn("优惠券不存在: couponId={}", couponId);
            return false;
        }
        
        // 2. 检查优惠券状态（0=未开始，1=进行中）
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            log.warn("优惠券不可领�? couponId={}, status={}", couponId, coupon.getStatus());
            return false;
        }
        
        // 3. 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidStart() != null && now.isBefore(coupon.getValidStart())) {
            log.warn("优惠券未到开始时�? couponId={}", couponId);
            return false;
        }
        if (coupon.getValidEnd() != null && now.isAfter(coupon.getValidEnd())) {
            log.warn("优惠券已过期: couponId={}", couponId);
            return false;
        }
        
        // 4. 检查库�?
        if (coupon.getTotalStock() != null && coupon.getReceivedCount() != null 
                && coupon.getReceivedCount() >= coupon.getTotalStock()) {
            log.warn("优惠券已领完: couponId={}", couponId);
            return false;
        }
        
        // 5. 更新领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() != null ? coupon.getReceivedCount() + 1 : 1);
        boolean success = this.updateById(coupon);
        
        if (success) {
            log.info("优惠券领取成�? couponId={}, userId={}", couponId, userId);
            
            // TODO: 6. 创建用户优惠券记录（通过CouponUserRecordService�?
            // couponUserRecordService.createRecord(couponId, userId);
            
            return true;
        } else {
            log.error("优惠券领取失�? couponId={}, userId={}", couponId, userId);
            return false;
        }
    }

    @Override
    public List<Coupon> listAvailableCoupons(Long userId) {
        log.info("查询用户可领取的优惠券列�? userId={}", userId);
        
        LocalDateTime now = LocalDateTime.now();
        
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, 1)  // 进行�?
               .and(w -> w.isNull(Coupon::getValidStart).or().le(Coupon::getValidStart, now))
               .and(w -> w.isNull(Coupon::getValidEnd).or().ge(Coupon::getValidEnd, now))
               .and(w -> w.isNull(Coupon::getTotalStock).or()
                       .apply("received_count < total_stock"))  // 还有库存
               .orderByDesc(Coupon::getCreateTime);
        
        return this.list(wrapper);
    }
}
