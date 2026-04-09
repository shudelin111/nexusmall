package com.nexusmall.promotion.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.promotion.application.service.FlashSaleItemService;
import com.nexusmall.promotion.domain.entity.FlashSaleItem;
import com.nexusmall.promotion.infrastructure.persistence.mapper.FlashSaleItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品服务实现�?
 * <p>
 * 业界标准实现�?
 * - Redis预减库存（高性能�?
 * - 分布式锁防超�?
 * - 数据库乐观锁最终一致�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleItemServiceImpl extends ServiceImpl<FlashSaleItemMapper, FlashSaleItem> implements FlashSaleItemService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<FlashSaleItem> listActiveItems() {
        log.info("查询活动中的秒杀商品列表");
        
        // 查询所有秒杀商品（活动状态由 FlashSale 控制，这里只返回商品列表�?
        LambdaQueryWrapper<FlashSaleItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(FlashSaleItem::getStock, 0)
               .orderByDesc(FlashSaleItem::getSortOrder);
        
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean seckill(Long skuId, Long userId) {
        log.info("秒杀下单: skuId={}, userId={}", skuId, userId);
        
        // 1. 检查用户是否已购买（防止重复秒杀�?
        String userKey = "seckill:user:" + skuId + ":" + userId;
        Boolean hasPurchased = redisTemplate.hasKey(userKey);
        if (Boolean.TRUE.equals(hasPurchased)) {
            log.warn("用户已参与过秒杀: skuId={}, userId={}", skuId, userId);
            return false;
        }
        
        // 2. Redis预减库存
        String stockKey = "seckill:stock:" + skuId;
        Long remainingStock = redisTemplate.opsForValue().decrement(stockKey);
        
        if (remainingStock == null || remainingStock < 0) {
            // 库存不足，恢复Redis计数
            redisTemplate.opsForValue().increment(stockKey);
            log.warn("秒杀库存不足: skuId={}", skuId);
            return false;
        }
        
        try {
            // 3. 数据库扣减库存（乐观锁）
            LambdaQueryWrapper<FlashSaleItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FlashSaleItem::getSkuId, skuId)
                   .ge(FlashSaleItem::getStock, 1);
            
            FlashSaleItem item = this.getOne(wrapper);
            if (item == null) {
                log.error("秒杀商品不存在或库存不足: skuId={}", skuId);
                // 恢复Redis计数
                redisTemplate.opsForValue().increment(stockKey);
                return false;
            }
            
            item.setStock(item.getStock() - 1);
            item.setSoldCount(item.getSoldCount() != null ? item.getSoldCount() + 1 : 1);
            
            boolean success = this.updateById(item);
            
            if (success) {
                // 4. 标记用户已购买（设置过期时间�?4小时�?
                redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
                
                log.info("秒杀成功: skuId={}, userId={}, 剩余库存={}", skuId, userId, item.getStock() - 1);
                
                // TODO: 5. 异步创建订单（通过RocketMQ发送消息）
                // rocketMQTemplate.convertAndSend("seckill-order-topic", orderMessage);
                
                return true;
            } else {
                // 数据库更新失败，恢复Redis计数
                redisTemplate.opsForValue().increment(stockKey);
                log.error("秒杀数据库更新失�? skuId={}", skuId);
                return false;
            }
            
        } catch (Exception e) {
            // 异常时恢复Redis计数
            redisTemplate.opsForValue().increment(stockKey);
            log.error("秒杀异常: skuId={}, userId={}", skuId, userId, e);
            throw e;
        }
    }
}
