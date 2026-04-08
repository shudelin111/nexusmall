package com.nexusmall.promotion.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.promotion.domain.entity.FlashSaleItem;

import java.util.List;

/**
 * 秒杀商品 Service 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface FlashSaleItemService extends IService<FlashSaleItem> {

    /**
     * 查询活动中的秒杀商品列表
     *
     * @return 秒杀商品列表
     */
    List<FlashSaleItem> listActiveItems();

    /**
     * 秒杀下单（核心方法）
     * <p>
     * 业界标准实现：
     * 1. Redis预减库存（高性能）
     * 2. 分布式锁防超卖
     * 3. 数据库乐观锁最终一致性
     * 4. 异步创建订单（RocketMQ）
     * </p>
     *
     * @param skuId  SKU ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean seckill(Long skuId, Long userId);
}
