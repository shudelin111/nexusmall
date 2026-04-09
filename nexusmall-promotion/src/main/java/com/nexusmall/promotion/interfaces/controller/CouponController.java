package com.nexusmall.promotion.interfaces.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.promotion.interfaces.dto.CreateCouponRequest;
import com.nexusmall.promotion.domain.entity.Coupon;
import com.nexusmall.promotion.application.service.CouponService;
import com.nexusmall.promotion.application.service.BloomFilterService;
import com.nexusmall.promotion.interfaces.dto.CouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券控制器（RESTful标准版）
 * <p>
 * RESTful资源设计�?
 * - GET    /coupons          - 查询可领取的优惠券列�?
 * - GET    /coupons/{id}     - 查询单个优惠券详�?
 * - POST   /coupons          - 创建优惠券（管理员）
 * - PUT    /coupons/{id}     - 更新优惠券（管理员）
 * - DELETE /coupons/{id}     - 删除优惠券（管理员）
 * - POST   /coupons/{id}/receive - 领取优惠�?
 * - GET    /users/{userId}/coupons - 查询用户已领取的优惠�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/coupons")  // RESTful资源路径：优惠券集合
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "优惠券管�?, description = "优惠券的创建、查询、领取等操作")
public class CouponController {

    private final CouponService couponService;
    private final BloomFilterService bloomFilterService;

    /**
     * 查询可领取的优惠券列�?
     *
     * @param userId 用户ID
     * @return 优惠券列�?
     */
    @GetMapping(value = "/", headers = "X-API-Version=v1")
    @Operation(summary = "查询可领取的优惠券列�?, description = "获取当前用户可领取的所有优惠券")
    public Result<List<Coupon>> listAvailableCoupons(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【查询可领取优惠券】userId={}", userId);
        List<Coupon> coupons = couponService.listAvailableCoupons(userId);
        return Result.success(coupons);
    }

    /**
     * 查询单个优惠券详�?
     *
     * @param id 优惠券ID
     * @return 优惠券详�?
     */
    @GetMapping(value = "/{id}", headers = "X-API-Version=v1")
    @Operation(summary = "查询优惠券详�?, description = "根据ID查询优惠券详细信�?)
    public Result<Coupon> getCouponById(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable Long id) {
        log.info("【查询优惠券详情】id={}", id);
        
        // 布隆过滤器校�?
        if (!bloomFilterService.mightContainCoupon(id)) {
            return Result.failure("404", "优惠券不存在");
        }
        
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.failure("404", "优惠券不存在");
        }
        return Result.success(coupon);
    }

    /**
     * 创建优惠券（管理员）
     *
     * @param request 优惠券信�?
     * @return 是否成功
     */
    @PostMapping(value = "/", headers = "X-API-Version=v1")
    @Operation(summary = "创建优惠�?, description = "管理员创建新的优惠券活动")
    @SentinelResource(value = "createCoupon", blockHandler = "handleBlock")
    public Result<Void> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("【创建优惠券】name={}, type={}", request.getName(), request.getType());
        
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(request, coupon);
        coupon.setCode(generateCouponCode()); // 生成唯一编码
        coupon.setStatus(0); // 未开�?
        coupon.setReceivedCount(0);
        
        boolean success = couponService.save(coupon);
        if (success) {
            // 添加到布隆过滤器
            bloomFilterService.addCoupon(coupon.getId());
        }
        
        return success ? Result.success() : Result.failure("500", "创建失败");
    }

    /**
     * 生成优惠券编�?
     */
    private String generateCouponCode() {
        return "CPN" + System.currentTimeMillis();
    }

    /**
     * 更新优惠券（管理员）
     *
     * @param id     优惠券ID
     * @param coupon 优惠券信�?
     * @return 是否成功
     */
    @PutMapping(value = "/{id}", headers = "X-API-Version=v1")
    @Operation(summary = "更新优惠�?, description = "管理员更新优惠券信息")
    public Result<Void> updateCoupon(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable Long id,
            @RequestBody Coupon coupon) {
        log.info("【更新优惠券】id={}, name={}", id, coupon.getName());
        coupon.setId(id);
        boolean success = couponService.updateById(coupon);
        return success ? Result.success() : Result.failure("500", "更新失败");
    }

    /**
     * 删除优惠券（管理员）
     *
     * @param id 优惠券ID
     * @return 是否成功
     */
    @DeleteMapping(value = "/{id}", headers = "X-API-Version=v1")
    @Operation(summary = "删除优惠�?, description = "管理员删除优惠券（逻辑删除�?)
    public Result<Void> deleteCoupon(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable Long id) {
        log.info("【删除优惠券】id={}", id);
        boolean success = couponService.removeById(id);
        return success ? Result.success() : Result.failure("500", "删除失败");
    }

    /**
     * 领取优惠�?
     *
     * @param id     优惠券ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/receive", headers = "X-API-Version=v1")
    @Operation(summary = "领取优惠�?, description = "用户领取指定优惠券，支持库存检查和限领控制")
    public Result<Void> receiveCoupon(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【领取优惠券】couponId={}, userId={}", id, userId);
        boolean success = couponService.receiveCoupon(id, userId);
        return success ? Result.success() : Result.failure("500", "领取失败");
    }
}
