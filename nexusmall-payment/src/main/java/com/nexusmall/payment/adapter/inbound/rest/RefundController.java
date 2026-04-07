package com.nexusmall.payment.adapter.inbound.rest;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.payment.application.service.RefundService;
import com.nexusmall.payment.interfaces.dto.request.CreateRefundRequest;
import com.nexusmall.payment.interfaces.dto.response.RefundResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 退款控制器
 * <p>
 * 提供退款申请、审核、执行等REST API
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/refunds")  // RESTful资源路径：退款单集合
@RequiredArgsConstructor
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
@Tag(name = "退款管理", description = "退款申请、审核、执行等接口")
public class RefundController {

    private final RefundService refundService;

    /**
     * 申请退款
     *
     * @param request 退款申请请求
     * @return 退款单号
     */
    @PostMapping(value = "/", headers = "X-API-Version=v1")
    @Operation(summary = "申请退款", description = "用户提交退款申请")
    public Result<Map<String, String>> applyRefund(@Valid @RequestBody CreateRefundRequest request) {
        log.info("【申请退款】paymentNo={}, amount={}", request.getPaymentNo(), request.getRefundAmount());
        
        String refundNo = refundService.applyRefund(request);
        
        Map<String, String> result = new HashMap<>();
        result.put("refundNo", refundNo);
        
        return Result.success(result);
    }

    /**
     * 审核退款
     *
     * @param refundNo 退款单号
     * @param auditRequest 审核请求
     * @return 审核结果
     */
    @PatchMapping(value = "/{refundNo}/audit", headers = "X-API-Version=v1")
    @Operation(summary = "审核退款", description = "管理员审核退款申请")
    public Result<Void> auditRefund(
            @Parameter(description = "退款单号", required = true)
            @PathVariable String refundNo,
            @RequestBody Map<String, Object> auditRequest) {
        log.info("【审核退款】refundNo={}", refundNo);
        
        Long auditorId = Long.valueOf(auditRequest.get("auditorId").toString());
        String auditorName = (String) auditRequest.get("auditorName");
        Boolean approved = (Boolean) auditRequest.get("approved");
        String remark = (String) auditRequest.get("remark");
        
        refundService.auditRefund(refundNo, auditorId, auditorName, approved, remark);
        return Result.success();
    }

    /**
     * 执行退款
     *
     * @param refundNo 退款单号
     * @return 执行结果
     */
    @PatchMapping(value = "/{refundNo}/execute", headers = "X-API-Version=v1")
    @Operation(summary = "执行退款", description = "执行已审核通过的退款")
    public Result<Void> executeRefund(
            @Parameter(description = "退款单号", required = true)
            @PathVariable String refundNo) {
        log.info("【执行退款】refundNo={}", refundNo);
        
        refundService.executeRefund(refundNo);
        return Result.success();
    }

    /**
     * 查询退款单
     *
     * @param refundNo 退款单号
     * @return 退款单详情
     */
    @GetMapping(value = "/{refundNo}", headers = "X-API-Version=v1")
    @Operation(summary = "查询退款单", description = "根据退款单号查询退款详情")
    public Result<RefundResponse> queryRefund(
            @Parameter(description = "退款单号", required = true)
            @PathVariable String refundNo) {
        log.info("【查询退款单】refundNo={}", refundNo);
        
        RefundResponse response = refundService.queryRefund(refundNo);
        return Result.success(response);
    }
}
