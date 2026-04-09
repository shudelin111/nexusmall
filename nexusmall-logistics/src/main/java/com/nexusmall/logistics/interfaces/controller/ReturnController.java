package com.nexusmall.logistics.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;
import com.nexusmall.logistics.application.service.LogisticsReturnApplyService;
import com.nexusmall.logistics.interfaces.dto.FillReturnLogisticsRequest;
import com.nexusmall.logistics.interfaces.dto.SubmitReturnApplyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退货管�?Controller
 * <p>
 * 业界标准�?
 * - 完整的退货流程管�?
 * - 支持退货申请、审核、物流跟�?
 * - RESTful API设计
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "退货管�?, description = "退货申请、审核、物流跟�?)
public class ReturnController {

    private final LogisticsReturnApplyService returnApplyService;

    /**
     * 提交退货申�?
     *
     * @param userId  用户ID（从Header获取�?
     * @param request 退货申请信�?
     * @return 退货申�?
     */
    @PostMapping(value = "/apply", headers = "X-API-Version=v1")
    @Operation(summary = "提交退货申�?, description = "用户提交退货申请，等待商家审核")
    public Result<LogisticsReturnApply> submitReturnApply(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Validated @RequestBody SubmitReturnApplyRequest request) {
        log.info("【提交退货申请】userId={}, orderSn={}", userId, request.getOrderSn());

        try {
            LogisticsReturnApply apply = returnApplyService.submitReturnApply(
                    request.getOrderSn(),
                    userId,
                    request.getReturnReason(),
                    request.getReturnDescription(),
                    request.getReturnImages()
            );
            return Result.success(apply);
        } catch (RuntimeException e) {
            log.error("【提交退货申请失败�?, e);
            return Result.failure("400", e.getMessage());
        }
    }

    /**
     * 查询用户的退货申请列�?
     *
     * @param userId 用户ID
     * @return 退货申请列�?
     */
    @GetMapping(value = "/my-returns", headers = "X-API-Version=v1")
    @Operation(summary = "查询我的退货申�?, description = "查询当前用户的所有退货申�?)
    public Result<List<LogisticsReturnApply>> listMyReturns(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【查询我的退货申请】userId={}", userId);
        List<LogisticsReturnApply> applies = returnApplyService.listByMemberId(userId);
        return Result.success(applies);
    }

    /**
     * 根据订单编号查询退货申�?
     *
     * @param orderSn 订单编号
     * @return 退货申请列�?
     */
    @GetMapping(value = "/order/{orderSn}", headers = "X-API-Version=v1")
    @Operation(summary = "根据订单查询退�?, description = "查询指定订单的退货申�?)
    public Result<List<LogisticsReturnApply>> listByOrderSn(
            @Parameter(description = "订单编号", required = true)
            @PathVariable String orderSn) {
        log.info("【查询订单退货】orderSn={}", orderSn);
        List<LogisticsReturnApply> applies = returnApplyService.listByOrderSn(orderSn);
        return Result.success(applies);
    }

    /**
     * 审核退货申请（同意�? 管理员接�?
     *
     * @param id 退货申请ID
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/approve", headers = "X-API-Version=v1")
    @Operation(summary = "同意退货申�?, description = "商家同意用户的退货申�?)
    public Result<Void> approveReturnApply(
            @Parameter(description = "退货申请ID", required = true)
            @PathVariable Long id) {
        log.info("【同意退货申请】id={}", id);
        boolean success = returnApplyService.approveReturnApply(id);
        return success ? Result.success() : Result.failure("500", "审核失败");
    }

    /**
     * 审核退货申请（拒绝�? 管理员接�?
     *
     * @param id     退货申请ID
     * @param reason 拒绝原因
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/reject", headers = "X-API-Version=v1")
    @Operation(summary = "拒绝退货申�?, description = "商家拒绝用户的退货申�?)
    public Result<Void> rejectReturnApply(
            @Parameter(description = "退货申请ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "拒绝原因", required = true)
            @RequestParam String reason) {
        log.info("【拒绝退货申请】id={}, reason={}", id, reason);
        boolean success = returnApplyService.rejectReturnApply(id, reason);
        return success ? Result.success() : Result.failure("500", "审核失败");
    }

    /**
     * 填写退货物流信�?
     *
     * @param id      退货申请ID
     * @param request 物流信息
     * @return 是否成功
     */
    @PutMapping(value = "/{id}/logistics", headers = "X-API-Version=v1")
    @Operation(summary = "填写退货物�?, description = "用户填写退货快递信�?)
    public Result<Void> fillReturnLogistics(
            @Parameter(description = "退货申请ID", required = true)
            @PathVariable Long id,
            @Validated @RequestBody FillReturnLogisticsRequest request) {
        log.info("【填写退货物流】id={}, expressNo={}", id, request.getExpressNo());
        boolean success = returnApplyService.fillReturnLogistics(
                id,
                request.getExpressCompany(),
                request.getExpressNo()
        );
        return success ? Result.success() : Result.failure("500", "填写失败");
    }

    /**
     * 确认收到退�?- 管理员接�?
     *
     * @param id 退货申请ID
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/confirm-receive", headers = "X-API-Version=v1")
    @Operation(summary = "确认收到退�?, description = "商家确认收到退货，触发退款流�?)
    public Result<Void> confirmReturnReceive(
            @Parameter(description = "退货申请ID", required = true)
            @PathVariable Long id) {
        log.info("【确认收到退货】id={}", id);
        boolean success = returnApplyService.confirmReturnReceive(id);
        return success ? Result.success() : Result.failure("500", "确认失败");
    }
}
