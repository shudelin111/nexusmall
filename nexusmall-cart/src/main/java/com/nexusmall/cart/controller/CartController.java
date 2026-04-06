package com.nexusmall.cart.controller;

import com.nexusmall.cart.dto.AddCartRequest;
import com.nexusmall.cart.service.CartService;
import com.nexusmall.cart.vo.CartSummaryVO;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 购物车控制器 V1（业界标准版）
 * <p>
 * API版本: v1
 * 通过 Header X-API-Version 进行版本控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/")
@ApiVersion("v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "购物车管理", description = "购物车增删改查、批量操作、统计汇总接口")
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车（增强版）
     * <p>
     * 核心特性：
     * - 幂等性保证（相同SKU只更新数量）
     * - 分布式锁（防止并发冲突）
     * - 商品校验（调用Product服务）
     * - 库存检查（实时验证）
     * - 限购逻辑（单次购买上限）
     * </p>
     */
    @PostMapping(value = "/items", headers = "X-API-Version=v1")
    @Operation(
        summary = "添加商品到购物车",
        description = "业界标准：幂等性保证 + 分布式锁 + 商品校验 + 库存检查 + 限购逻辑"
    )
    public Result<Void> addToCart(
            @Parameter(description = "用户ID（从JWT解析）", hidden = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "添加购物车请求", required = true)
            @Valid @RequestBody AddCartRequest request) {
        
        log.info("【添加购物车】userId={}, skuId={}, quantity={}", userId, request.getSkuId(), request.getQuantity());
        request.setUserId(userId);
        cartService.addToCart(request);
        return Result.success();
    }

    /**
     * 查询购物车汇总（增强版）
     * <p>
     * 返回完整的购物车信息，包括：
     * - 有效商品列表
     * - 失效商品列表
     * - 统计信息（总价、总数量、优惠金额）
     * - 提示信息（价格变动、库存不足等）
     * </p>
     */
    @GetMapping(value = "/summary", headers = "X-API-Version=v1")
    @Operation(
        summary = "查询购物车汇总",
        description = "业界标准：包含有效/失效商品分类 + 统计信息 + 价格变动提示"
    )
    public Result<CartSummaryVO> getCartSummary(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("【查询购物车汇总】userId={}", userId);
        CartSummaryVO summary = cartService.getCartSummary(userId);
        return Result.success(summary);
    }

    /**
     * 查询购物车原始数据（兼容旧版）
     */
    @GetMapping(value = "/items", headers = "X-API-Version=v1")
    @Operation(summary = "查询购物车原始数据", description = "获取用户购物车所有商品（未加工）")
    public Result<List<?>> getCartItems(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("【查询购物车】userId={}", userId);
        // 建议使用 /summary 接口，此接口仅用于兼容
        CartSummaryVO summary = cartService.getCartSummary(userId);
        return Result.success(summary.getValidItems());
    }

    /**
     * 更新商品数量（增强版）
     * <p>
     * 核心特性：
     * - 分布式锁（防止并发冲突）
     * - 库存检查（实时验证）
     * - 限购逻辑（单次购买上限）
     * </p>
     */
    @PutMapping(value = "/items/{skuId}", headers = "X-API-Version=v1")
    @Operation(
        summary = "更新商品数量",
        description = "业界标准：分布式锁 + 库存检查 + 限购逻辑"
    )
    public Result<Void> updateQuantity(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "SKU ID", required = true)
            @PathVariable Long skuId,
            @Parameter(description = "新数量", required = true, example = "2")
            @RequestParam Integer quantity) {
        
        log.info("【更新数量】userId={}, skuId={}, quantity={}", userId, skuId, quantity);
        cartService.updateQuantity(userId, skuId, quantity);
        return Result.success();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping(value = "/items/{skuId}", headers = "X-API-Version=v1")
    @Operation(summary = "删除购物车商品", description = "从购物车中移除指定商品")
    public Result<Void> removeFromCart(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "SKU ID", required = true)
            @PathVariable Long skuId) {
        
        log.info("【删除购物车】userId={}, skuId={}", userId, skuId);
        cartService.removeFromCart(userId, skuId);
        return Result.success();
    }

    /**
     * 批量删除购物车商品
     */
    @DeleteMapping(value = "/items/batch", headers = "X-API-Version=v1")
    @Operation(
        summary = "批量删除购物车商品",
        description = "一次性删除多个商品，提升用户体验"
    )
    public Result<Void> batchRemoveFromCart(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "SKU ID列表", required = true)
            @RequestParam List<Long> skuIds) {
        
        log.info("【批量删除】userId={}, skuIds={}", userId, skuIds);
        cartService.batchRemoveFromCart(userId, skuIds);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping(value = "/items", headers = "X-API-Version=v1")
    @Operation(summary = "清空购物车", description = "删除购物车中所有商品")
    public Result<Void> clearCart(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("【清空购物车】userId={}", userId);
        cartService.clearCart(userId);
        return Result.success();
    }

    /**
     * 选中/取消选中商品
     */
    @PutMapping(value = "/items/{skuId}/selected", headers = "X-API-Version=v1")
    @Operation(
        summary = "选中/取消选中商品",
        description = "切换单个商品的选中状态"
    )
    public Result<Void> toggleSelected(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "SKU ID", required = true)
            @PathVariable Long skuId,
            @Parameter(description = "是否选中: 0-否 1-是", required = true, example = "1")
            @RequestParam Integer selected) {
        
        log.info("【切换选中状态】userId={}, skuId={}, selected={}", userId, skuId, selected);
        cartService.toggleSelected(userId, skuId, selected);
        return Result.success();
    }

    /**
     * 全选/全不选
     */
    @PutMapping(value = "/items/selectAll", headers = "X-API-Version=v1")
    @Operation(
        summary = "全选/全不选",
        description = "一键选中或取消选中所有商品"
    )
    public Result<Void> toggleSelectAll(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "是否全选: 0-全不选 1-全选", required = true, example = "1")
            @RequestParam Integer selected) {
        
        log.info("【全选/全不选】userId={}, selected={}", userId, selected);
        cartService.toggleSelectAll(userId, selected);
        return Result.success();
    }

    /**
     * 合并匿名购物车到登录账户
     * <p>
     * 业界标准：用户登录后，将匿名用户购物车合并到登录账户
     * - 相同SKU：数量累加
     * - 不同SKU：直接追加
     * </p>
     */
    @PostMapping(value = "/merge", headers = "X-API-Version=v1")
    @Operation(
        summary = "合并匿名购物车",
        description = "业界标准：用户登录后自动合并匿名购物车到登录账户"
    )
    public Result<Void> mergeAnonymousCart(
            @Parameter(description = "登录用户ID", required = true)
            @RequestHeader("X-User-ID") Long loggedInUserId,
            @Parameter(description = "匿名用户ID（临时ID）", required = true)
            @RequestParam Long anonymousUserId) {
        
        log.info("【合并购物车】loggedInUserId={}, anonymousUserId={}", loggedInUserId, anonymousUserId);
        cartService.mergeCart(loggedInUserId, anonymousUserId);
        return Result.success();
    }
}
