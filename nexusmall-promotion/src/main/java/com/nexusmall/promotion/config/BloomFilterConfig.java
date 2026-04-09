package com.nexusmall.promotion.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * 布隆过滤器配置类
 * <p>
 * 业界标准：防止缓存穿透，保护数据?
 * - 误判率：0.01%（万分之一?
 * - 预期元素数量?00?
 * - 适用场景：秒杀商品ID、优惠券ID校验
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Configuration
public class BloomFilterConfig {

    /**
     * 秒杀商品布隆过滤?
     * <p>
     * 用于快速判断SKU ID是否存在于秒杀活动?
     * - 如果返回false：一定不存在，直接拒绝请?
     * - 如果返回true：可能存在，继续后续校验
     * </p>
     *
     * @return 布隆过滤?
     */
    @Bean("seckillSkuBloomFilter")
    public BloomFilter<String> seckillSkuBloomFilter() {
        // 预期插入100万个SKU ID，误判率0.01%
        BloomFilter<String> bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(StandardCharsets.UTF_8),
            1_000_000,  // 预期元素数量
            0.0001      // 误判率（0.01%?
        );
        
        log.info("【布隆过滤器初始化】秒杀商品布隆过滤器创建成功，预期容量=100万，误判?0.01%");
        return bloomFilter;
    }

    /**
     * 优惠券布隆过滤器
     * <p>
     * 用于快速判断优惠券ID是否有效
     * </p>
     *
     * @return 布隆过滤?
     */
    @Bean("couponBloomFilter")
    public BloomFilter<String> couponBloomFilter() {
        // 预期插入10万个优惠券ID，误判率0.01%
        BloomFilter<String> bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(StandardCharsets.UTF_8),
            100_000,   // 预期元素数量
            0.0001     // 误判率（0.01%?
        );
        
        log.info("【布隆过滤器初始化】优惠券布隆过滤器创建成功，预期容量=10万，误判?0.01%");
        return bloomFilter;
    }
}
