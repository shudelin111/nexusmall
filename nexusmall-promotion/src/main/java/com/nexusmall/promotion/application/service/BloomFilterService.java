package com.nexusmall.promotion.application.service;

import com.google.common.hash.BloomFilter;
import com.nexusmall.promotion.domain.constants.PromotionConstants;
import com.nexusmall.promotion.domain.entity.Coupon;
import com.nexusmall.promotion.domain.entity.FlashSaleItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 布隆过滤器服务
 * <p>
 * 业界标准：
 * 1. 应用启动时预热布隆过滤器
 * 2. 秒杀请求先经过布隆过滤器校验
 * 3. 防止缓存穿透，保护数据库
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
public class BloomFilterService {

    private final BloomFilter<String> seckillSkuBloomFilter;
    private final BloomFilter<String> couponBloomFilter;
    private final FlashSaleItemService flashSaleItemService;
    private final CouponService couponService;
    private final StringRedisTemplate redisTemplate;

    public BloomFilterService(
            @Qualifier("seckillSkuBloomFilter") BloomFilter<String> seckillSkuBloomFilter,
            @Qualifier("couponBloomFilter") BloomFilter<String> couponBloomFilter,
            FlashSaleItemService flashSaleItemService,
            CouponService couponService,
            StringRedisTemplate redisTemplate) {
        this.seckillSkuBloomFilter = seckillSkuBloomFilter;
        this.couponBloomFilter = couponBloomFilter;
        this.flashSaleItemService = flashSaleItemService;
        this.couponService = couponService;
        this.redisTemplate = redisTemplate;
    }

    private static final String BLOOM_FILTER_INIT_KEY = "bloom:filter:init";

    /**
     * 应用启动时初始化布隆过滤器
     * <p>
     * 从数据库加载所有有效的SKU ID和优惠券ID到布隆过滤器
     * </p>
     */
    @PostConstruct
    public void initBloomFilter() {
        // 防止重复初始化（分布式环境下）
        Boolean isInit = redisTemplate.opsForValue().setIfAbsent(BLOOM_FILTER_INIT_KEY, "1", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isInit)) {
            log.info("【布隆过滤器】已由其他实例初始化，跳过");
            return;
        }

        log.info("【布隆过滤器】开始初始化...");

        try {
            // 1. 初始化秒杀商品布隆过滤器
            initSeckillSkuBloomFilter();

            // 2. 初始化优惠券布隆过滤器
            initCouponBloomFilter();

            log.info("【布隆过滤器】初始化完成");
        } catch (Exception e) {
            log.error("【布隆过滤器】初始化失败", e);
            // 删除初始化标记，允许重试
            redisTemplate.delete(BLOOM_FILTER_INIT_KEY);
        }
    }

    /**
     * 初始化秒杀商品布隆过滤器
     */
    private void initSeckillSkuBloomFilter() {
        log.info("【布隆过滤器】加载秒杀商品SKU ID...");

        List<FlashSaleItem> items = flashSaleItemService.lambdaQuery()
                .gt(FlashSaleItem::getStock, 0)  // 有库存
                .list();

        for (FlashSaleItem item : items) {
            seckillSkuBloomFilter.put(String.valueOf(item.getSkuId()));
        }

        log.info("【布隆过滤器】秒杀商品SKU ID加载完成，数量={}", items.size());
    }

    /**
     * 初始化优惠券布隆过滤器
     */
    private void initCouponBloomFilter() {
        log.info("【布隆过滤器】加载优惠券ID...");

        List<Coupon> coupons = couponService.lambdaQuery()
                .in(Coupon::getStatus, 0, 1)  // 未开始或进行中
                .list();

        for (Coupon coupon : coupons) {
            couponBloomFilter.put(String.valueOf(coupon.getId()));
        }

        log.info("【布隆过滤器】优惠券ID加载完成，数量={}", coupons.size());
    }

    /**
     * 判断SKU ID是否可能在秒杀活动中
     * <p>
     * - 返回false：一定不存在，直接拒绝
     * - 返回true：可能存在，继续后续校验
     * </p>
     *
     * @param skuId SKU ID
     * @return 是否存在
     */
    public boolean mightContainSeckillSku(Long skuId) {
        return seckillSkuBloomFilter.mightContain(String.valueOf(skuId));
    }

    /**
     * 判断优惠券ID是否可能有效
     *
     * @param couponId 优惠券ID
     * @return 是否存在
     */
    public boolean mightContainCoupon(Long couponId) {
        return couponBloomFilter.mightContain(String.valueOf(couponId));
    }

    /**
     * 添加SKU ID到布隆过滤器（新增秒杀商品时调用）
     *
     * @param skuId SKU ID
     */
    public void addSeckillSku(Long skuId) {
        seckillSkuBloomFilter.put(String.valueOf(skuId));
        log.info("【布隆过滤器】添加秒杀商品SKU ID: {}", skuId);
    }

    /**
     * 添加优惠券ID到布隆过滤器（新增优惠券时调用）
     *
     * @param couponId 优惠券ID
     */
    public void addCoupon(Long couponId) {
        couponBloomFilter.put(String.valueOf(couponId));
        log.info("【布隆过滤器】添加优惠券ID: {}", couponId);
    }

    /**
     * 手动刷新布隆过滤器（管理后台调用）
     */
    public void refreshBloomFilter() {
        log.warn("【布隆过滤器】手动刷新开始...");
        
        // 清除Redis初始化标记
        redisTemplate.delete(BLOOM_FILTER_INIT_KEY);
        
        // 重新初始化
        initBloomFilter();
        
        log.info("【布隆过滤器】手动刷新完成");
    }
}
