package com.nexusmall.promotion.interfaces.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.promotion.domain.entity.FlashSaleItem;
import com.nexusmall.promotion.application.service.FlashSaleItemService;
import com.nexusmall.promotion.application.service.BloomFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器（RESTful标准版）
 * <p>
 * RESTful资源设计：
 * - GET    /flash-sales/items           - 查询秒杀商品列表
 * - GET    /flash-sales/items/{skuId}   - 查询单个秒杀商品详情
 * - POST   /flash-sales/items/{skuId}/seckill - 参与秒杀
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/flash-sales")  // RESTful资源路径：秒杀活动集合
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "秒杀管理", description = "秒杀活动的查询、参与等操作")
public class FlashSaleController {

    private final FlashSaleItemService flashSaleItemService;
    private final BloomFilterService bloomFilterService;

    /**
     * 查询秒杀商品列表
     *
     * @return 秒杀商品列表
     */
    @GetMapping(value = "/items", headers = "X-API-Version=v1")
    @Operation(summary = "查询秒杀商品列表", description = "获取当前正在进行的所有秒杀商品")
    public Result<List<FlashSaleItem>> listActiveItems() {
        log.info("【查询秒杀商品列表】");
        List<FlashSaleItem> items = flashSaleItemService.listActiveItems();
        return Result.success(items);
    }

    /**
     * 查询单个秒杀商品详情
     *
     * @param skuId SKU ID
     * @return 秒杀商品详情
     */
    @GetMapping(value = "/items/{skuId}", headers = "X-API-Version=v1")
    @Operation(summary = "查询秒杀商品详情", description = "根据SKU ID查询秒杀商品详细信息")
    public Result<FlashSaleItem> getItemBySkuId(
            @Parameter(description = "SKU ID", required = true)
            @PathVariable Long skuId) {
        log.info("【查询秒杀商品详情】skuId={}", skuId);
        FlashSaleItem item = flashSaleItemService.lambdaQuery()
                .eq(FlashSaleItem::getSkuId, skuId)
                .one();
        
        if (item == null) {
            return Result.failure("404", "秒杀商品不存在");
        }
        return Result.success(item);
    }

    /**
     * 参与秒杀（核心接口）
     * <p>
     * 业界标准：
     * - Redis预减库存（抗高并发）
     * - 分布式锁防超卖
     * - 数据库乐观锁最终一致性
     * - 异步创建订单
     * - Sentinel限流降级
     * </p>
     *
     * @param skuId  SKU ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @PostMapping(value = "/items/{skuId}/seckill", headers = "X-API-Version=v1")
    @Operation(
        summary = "参与秒杀", 
        description = "业界标准：布隆过滤器 + Redis预减库存 + 分布式锁 + 数据库乐观锁 + 异步订单 + Sentinel限流"
    )
    @SentinelResource(value = "seckill", blockHandler = "handleSeckillBlock")
    public Result<Void> seckill(
            @Parameter(description = "SKU ID", required = true)
            @PathVariable Long skuId,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【参与秒杀】skuId={}, userId={}", skuId, userId);
        
        // ==================== 第零层：布隆过滤器校验（防止缓存穿透）====================
        if (!bloomFilterService.mightContainSeckillSku(skuId)) {
            log.warn("【秒杀拦截】SKU ID不存在于布隆过滤器中，直接拒绝: skuId={}", skuId);
            return Result.failure("404", "秒杀商品不存在");
        }
        
        boolean success = flashSaleItemService.seckill(skuId, userId);
        return success ? Result.success() : Result.failure("500", "秒杀失败");
    }

    /**
     * 秒杀限流降级处理
     */
    public Result<Void> handleSeckillBlock(Long skuId, Long userId, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("【秒杀限流】skuId={}, userId={}, exception={}", skuId, userId, ex.getClass().getSimpleName());
        return Result.failure("429", "系统繁忙，请稍后重试");
    }
}
