package com.nexusmall.promotion.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.promotion.application.service.CouponUserRecordService;
import com.nexusmall.promotion.interfaces.dto.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户优惠券Controller
 * <p>
 * 业界标准：提供完整的用户优惠券管理接口
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/user-coupons")  // RESTful资源路径：用户优惠券集合
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "用户优惠券管理", description = "查询、使用、锁定、释放用户优惠券")
public class UserCouponController {

    private final CouponUserRecordService couponUserRecordService;

    /**
     * 查询用户的优惠券列表
     *
     * @param userId    用户ID
     * @param useStatus 使用状态（0-未使用,1-已使用,2-已过期,3-已锁定，null表示全部）
     * @return 优惠券列表
     */
    @GetMapping(value = "/", headers = "X-API-Version=v1")
    @Operation(summary = "查询用户优惠券列表", description = "支持按状态筛选：未使用、已使用、已过期、已锁定")
    public Result<List<UserCouponVO>> listUserCoupons(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "使用状态（0-未使用,1-已使用,2-已过期,3-已锁定，null表示全部）")
            @RequestParam(required = false) Integer useStatus) {
        log.info("【查询用户优惠券列表】userId={}, useStatus={}", userId, useStatus);
        List<UserCouponVO> coupons = couponUserRecordService.listUserCoupons(userId, useStatus);
        return Result.success(coupons);
    }

    /**
     * 核销优惠券（下单时使用）
     *
     * @param recordId 领取记录ID
     * @param orderId  订单ID
     * @return 是否成功
     */
    @PostMapping(value = "/{recordId}/use", headers = "X-API-Version=v1")
    @Operation(summary = "核销优惠券", description = "订单支付时调用，将优惠券标记为已使用")
    public Result<Void> useCoupon(
            @Parameter(description = "领取记录ID", required = true)
            @PathVariable Long recordId,
            @Parameter(description = "订单ID", required = true)
            @RequestParam Long orderId) {
        log.info("【核销优惠券】recordId={}, orderId={}", recordId, orderId);
        boolean success = couponUserRecordService.useCoupon(recordId, orderId);
        return success ? Result.success() : Result.failure("500", "核销失败");
    }

    /**
     * 锁定优惠券（下单未支付时）
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    @PostMapping(value = "/{recordId}/lock", headers = "X-API-Version=v1")
    @Operation(summary = "锁定优惠券", description = "创建订单但未支付时调用，防止优惠券被其他订单使用")
    public Result<Void> lockCoupon(
            @Parameter(description = "领取记录ID", required = true)
            @PathVariable Long recordId) {
        log.info("【锁定优惠券】recordId={}", recordId);
        boolean success = couponUserRecordService.lockCoupon(recordId);
        return success ? Result.success() : Result.failure("500", "锁定失败");
    }

    /**
     * 释放优惠券（取消订单或支付失败时）
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    @PostMapping(value = "/{recordId}/release", headers = "X-API-Version=v1")
    @Operation(summary = "释放优惠券", description = "订单取消或支付失败时调用，恢复优惠券为未使用状态")
    public Result<Void> releaseCoupon(
            @Parameter(description = "领取记录ID", required = true)
            @PathVariable Long recordId) {
        log.info("【释放优惠券】recordId={}", recordId);
        boolean success = couponUserRecordService.releaseCoupon(recordId);
        return success ? Result.success() : Result.failure("500", "释放失败");
    }

    /**
     * 退款回退优惠券
     *
     * @param recordId 领取记录ID
     * @return 是否成功
     */
    @PostMapping(value = "/{recordId}/refund", headers = "X-API-Version=v1")
    @Operation(summary = "退款回退优惠券", description = "订单退款时调用，如果优惠券未过期则恢复为未使用状态")
    public Result<Void> refundCoupon(
            @Parameter(description = "领取记录ID", required = true)
            @PathVariable Long recordId) {
        log.info("【退款回退优惠券】recordId={}", recordId);
        boolean success = couponUserRecordService.refundCoupon(recordId);
        return success ? Result.success() : Result.failure("500", "回退失败");
    }
}
