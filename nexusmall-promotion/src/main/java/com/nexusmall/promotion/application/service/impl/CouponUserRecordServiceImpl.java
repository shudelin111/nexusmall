package com.nexusmall.promotion.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.promotion.application.service.CouponUserRecordService;
import com.nexusmall.promotion.domain.entity.CouponUserRecord;
import com.nexusmall.promotion.infrastructure.persistence.mapper.CouponUserRecordMapper;
import com.nexusmall.promotion.interfaces.dto.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户优惠券领取记录服务实现类
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponUserRecordServiceImpl extends ServiceImpl<CouponUserRecordMapper, CouponUserRecord> implements CouponUserRecordService {

    @Override
    public List<UserCouponVO> listUserCoupons(Long userId, Integer useStatus) {
        log.info("查询用户优惠券列? userId={}, useStatus={}", userId, useStatus);
        
        LambdaQueryWrapper<CouponUserRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponUserRecord::getUserId, userId);
        
        if (useStatus != null) {
            wrapper.eq(CouponUserRecord::getUseStatus, useStatus);
        }
        
        wrapper.orderByDesc(CouponUserRecord::getReceiveTime);
        
        List<CouponUserRecord> records = this.list(wrapper);
        
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(Long recordId, Long orderId) {
        log.info("核销优惠? recordId={}, orderId={}", recordId, orderId);
        
        LambdaUpdateWrapper<CouponUserRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CouponUserRecord::getId, recordId)
               .eq(CouponUserRecord::getUseStatus, 0)  // 未使?
               .set(CouponUserRecord::getUseStatus, 1)  // 已使?
               .set(CouponUserRecord::getOrderId, orderId)
               .set(CouponUserRecord::getUseTime, LocalDateTime.now());
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("优惠券核销成功: recordId={}", recordId);
        } else {
            log.warn("优惠券核销失败: recordId={}", recordId);
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockCoupon(Long recordId) {
        log.info("锁定优惠? recordId={}", recordId);
        
        LambdaUpdateWrapper<CouponUserRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CouponUserRecord::getId, recordId)
               .eq(CouponUserRecord::getUseStatus, 0)  // 未使?
               .set(CouponUserRecord::getUseStatus, 2);  // 已锁?
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("优惠券锁定成? recordId={}", recordId);
        } else {
            log.warn("优惠券锁定失? recordId={}", recordId);
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseCoupon(Long recordId) {
        log.info("释放优惠? recordId={}", recordId);
        
        LambdaUpdateWrapper<CouponUserRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CouponUserRecord::getId, recordId)
               .eq(CouponUserRecord::getUseStatus, 2)  // 已锁?
               .set(CouponUserRecord::getUseStatus, 0);  // 未使?
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("优惠券释放成? recordId={}", recordId);
        } else {
            log.warn("优惠券释放失? recordId={}", recordId);
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundCoupon(Long recordId) {
        log.info("回退优惠? recordId={}", recordId);
        
        LambdaUpdateWrapper<CouponUserRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CouponUserRecord::getId, recordId)
               .in(CouponUserRecord::getUseStatus, 1, 2)  // 已使用或已锁?
               .set(CouponUserRecord::getUseStatus, 0)  // 未使?
               .set(CouponUserRecord::getOrderId, null)
               .set(CouponUserRecord::getUseTime, null);
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("优惠券回退成功: recordId={}", recordId);
        } else {
            log.warn("优惠券回退失败: recordId={}", recordId);
        }
        
        return success;
    }

    @Override
    public Integer countByUserIdAndCouponId(Long userId, Long couponId) {
        log.info("统计用户已领取的某优惠券数量: userId={}, couponId={}", userId, couponId);
        
        LambdaQueryWrapper<CouponUserRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponUserRecord::getUserId, userId)
               .eq(CouponUserRecord::getCouponId, couponId);
        
        return Math.toIntExact(this.count(wrapper));
    }

    /**
     * Entity ?VO
     */
    private UserCouponVO convertToVO(CouponUserRecord record) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
