package com.nexusmall.promotion.application.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nexusmall.promotion.domain.entity.Coupon;
import com.nexusmall.promotion.domain.entity.FlashSale;
import com.nexusmall.promotion.domain.enums.ActivityStatusEnum;
import com.nexusmall.promotion.application.service.CouponService;
import com.nexusmall.promotion.application.service.FlashSaleItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 营销活动定时任务
 * <p>
 * 业界标准：自动流转活动状态，避免人工干预
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionScheduleTask {

    private final CouponService couponService;
    private final FlashSaleItemService flashSaleItemService;

    /**
     * 每分钟检查活动状态（未开始→进行中）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivityStart() {
        log.info("【定时任务】检查活动开始");

        LocalDateTime now = LocalDateTime.now();

        // 1. 更新优惠券状态：未开始→进行中
        LambdaUpdateWrapper<Coupon> couponWrapper = new LambdaUpdateWrapper<>();
        couponWrapper.eq(Coupon::getStatus, ActivityStatusEnum.NOT_STARTED.getCode())
                .le(Coupon::getValidStart, now)
                .set(Coupon::getStatus, ActivityStatusEnum.IN_PROGRESS.getCode());
        couponService.update(couponWrapper);

        // 2. 更新秒杀活动状态：未开始→进行中
        // TODO: 实现秒杀活动状态更新

        log.info("【定时任务】活动状态检查完成");
    }

    /**
     * 每小时检查优惠券过期
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkCouponExpire() {
        log.info("【定时任务】检查优惠券过期");

        LocalDateTime now = LocalDateTime.now();

        // 更新已过期的优惠券状态：进行中→已结束
        LambdaUpdateWrapper<Coupon> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Coupon::getStatus, ActivityStatusEnum.IN_PROGRESS.getCode())
                .lt(Coupon::getValidEnd, now)
                .set(Coupon::getStatus, ActivityStatusEnum.ENDED.getCode());
        couponService.update(wrapper);

        log.info("【定时任务】优惠券过期检查完成");
    }

    /**
     * 每天凌晨2点聚合统计数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateStatistics() {
        log.info("【定时任务】聚合昨日统计数据");

        // TODO: 从Redis读取昨日数据，聚合到数据库
        // 1. 统计优惠券领取使用数据
        // 2. 统计秒杀成交数据
        // 3. 计算转化率、ROI等指标

        log.info("【定时任务】统计数据聚合完成");
    }
}
