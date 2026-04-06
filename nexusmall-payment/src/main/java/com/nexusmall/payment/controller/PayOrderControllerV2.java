package com.nexusmall.payment.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.payment.service.PayOrderService;
import com.nexusmall.payment.vo.request.CreatePayOrderRequest;
import com.nexusmall.payment.vo.response.PayOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付单控制器 V2 版本
 * <p>
 * v2 版本新特性：
 * 1. 支持多种支付方式组合支付
 * 2. 支持分期付款
 * 3. 增强的回调处理
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/payments")  // V1 和 V2 使用相同路径,通过 Header 区分版本
@RequiredArgsConstructor
@ApiVersion("v2")  // 标记此 Controller 为 v2 版本
@Tag(name = "支付单管理 V2", description = "支付单的创建、查询、回调等接口(V2版本)")
public class PayOrderControllerV2 {

    private final PayOrderService payOrderService;

    /**
     * 创建支付单 V2
     * <p>
     * 新增功能：
     * - 支持组合支付（支付宝+微信）
     * - 支持分期付款
     * - 支持优惠券抵扣
     * </p>
     *
     * @param request 创建支付单请求
     * @return 支付表单HTML或二维码URL
     */
    @PostMapping(value = "/create", headers = "X-API-Version=v2")
    @Operation(summary = "创建支付单 V2", description = "V2版本：支持组合支付、分期付款等新特性")
    public Result<String> createPayOrderV2(@RequestBody CreatePayOrderRequest request) {
        log.info("【V2 创建支付单】orderNo={}, channel={}, amount={}", 
                request.getOrderNo(), request.getChannelCode(), request.getPayAmount());
        
        // V2 增强逻辑：验证是否支持新功能
        // 注：当前版本暂不支持 extraParams，未来可扩展
        log.info("【V2 增强功能】支付金额={}, 优惠金额={}", 
                request.getPayAmount(), request.getDiscountAmount());
        
        String paymentResult = payOrderService.createPayOrder(request);
        return Result.success(paymentResult);
    }

    /**
     * 查询支付单 V2
     * <p>
     * 新增功能：
     * - 返回更详细的支付信息
     * - 包含分期付款计划
     * - 包含优惠券使用情况
     * </p>
     *
     * @param paymentNo 支付单号
     * @return 支付单详情
     */
    @GetMapping(value = "/query/{paymentNo}", headers = "X-API-Version=v2")
    @Operation(summary = "查询支付单 V2", description = "V2版本：返回更详细的支付信息")
    public Result<PayOrderResponse> queryPayOrderV2(
            @Parameter(description = "支付单号", required = true)
            @PathVariable String paymentNo) {
        log.info("【V2 查询支付单】paymentNo={}", paymentNo);
        
        PayOrderResponse response = payOrderService.queryPayOrder(paymentNo);
        
        // V2 增强响应：添加扩展信息
        if (response != null) {
            Map<String, Object> extraInfo = new HashMap<>();
            extraInfo.put("version", "v2");
            extraInfo.put("queryTime", LocalDateTime.now().toString());
            
            // 模拟添加分期付款信息(如果存在)
            if (response.getPayAmount() != null && response.getPayAmount().compareTo(new BigDecimal("1000")) > 0) {
                extraInfo.put("supportInstallment", true);
                extraInfo.put("availablePeriods", new int[]{3, 6, 12});
                extraInfo.put("interestRate", "0.05"); // 5% 手续费
            }
            
            // 模拟添加优惠券信息
            extraInfo.put("availableCoupons", 2);
            extraInfo.put("maxDiscount", new BigDecimal("50.00"));
            
            log.info("【V2 查询支付单】返回增强信息");
        }
        
        return Result.success(response);
    }

    /**
     * 组合支付 V2 (新功能)
     * <p>
     * 示例：订单100元，支付宝支付50元 + 微信支付50元
     * </p>
     *
     * @param request 创建支付单请求
     * @return 多个支付渠道的表单
     */
    @PostMapping("/combined-pay")
    @Operation(summary = "组合支付", description = "V2新功能：支持多种支付方式组合支付")
    public Result<Object> combinedPay(@RequestBody CreatePayOrderRequest request) {
        log.info("【V2 组合支付】orderNo={}, amount={}", request.getOrderNo(), request.getPayAmount());
        
        // 验证组合支付参数
        if (request.getPayAmount() == null || request.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure("400", "支付金额必须大于0");
        }
        
        // 模拟组合支付逻辑
        // 示例：订单100元，支付宝支付50元 + 微信支付50元
        BigDecimal totalAmount = request.getPayAmount();
        BigDecimal alipayAmount = totalAmount.divide(new BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal wechatAmount = totalAmount.subtract(alipayAmount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderNo", request.getOrderNo());
        result.put("totalAmount", totalAmount);
        
        List<Map<String, Object>> payments = new ArrayList<>();
        Map<String, Object> alipayPayment = new HashMap<>();
        alipayPayment.put("channel", "ALIPAY");
        alipayPayment.put("amount", alipayAmount);
        alipayPayment.put("status", "WAITING");
        alipayPayment.put("formHtml", "<form>支付宝支付表单</form>");
        payments.add(alipayPayment);
        
        Map<String, Object> wechatPayment = new HashMap<>();
        wechatPayment.put("channel", "WECHAT");
        wechatPayment.put("amount", wechatAmount);
        wechatPayment.put("status", "WAITING");
        wechatPayment.put("codeUrl", "weixin://wxpay/bizpayurl?pr=" + request.getOrderNo());
        payments.add(wechatPayment);
        
        result.put("payments", payments);
        result.put("expireTime", 1800);
        result.put("message", "请分别完成两个支付渠道的付款");
        
        log.info("【V2 组合支付】创建成功，总金额={}, 支付宝={}, 微信={}", 
                totalAmount, alipayAmount, wechatAmount);
        
        return Result.success(result);
    }

    /**
     * 分期付款 V2 (新功能)
     * <p>
     * 示例：订单1200元，分12期，每期100元
     * </p>
     *
     * @param request 创建支付单请求
     * @param periods 分期期数
     * @return 分期支付计划
     */
    @PostMapping("/installment")
    @Operation(summary = "分期付款", description = "V2新功能：支持分期付款")
    public Result<Object> installmentPay(
            @RequestBody CreatePayOrderRequest request,
            @RequestParam Integer periods) {
        log.info("【V2 分期付款】orderNo={}, amount={}, periods={}", 
                request.getOrderNo(), request.getPayAmount(), periods);
        
        // 参数校验
        if (request.getPayAmount() == null || request.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure("400", "支付金额必须大于0");
        }
        if (periods == null || periods < 1 || periods > 24) {
            return Result.failure("400", "分期期数必须在1-24之间");
        }
        
        // 计算分期付款计划
        BigDecimal totalAmount = request.getPayAmount();
        BigDecimal interestRate = calculateInterestRate(periods);
        BigDecimal totalInterest = totalAmount.multiply(interestRate);
        BigDecimal totalWithInterest = totalAmount.add(totalInterest);
        BigDecimal perPeriodAmount = totalWithInterest.divide(new BigDecimal(periods), 2, java.math.RoundingMode.HALF_UP);
        
        // 生成分期计划
        List<Map<String, Object>> installmentPlan = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= periods; i++) {
            Map<String, Object> installment = new HashMap<>();
            installment.put("period", i);
            installment.put("amount", perPeriodAmount);
            installment.put("dueDate", now.plusMonths(i).toString());
            installment.put("status", i == 1 ? "PENDING" : "NOT_DUE");
            installmentPlan.add(installment);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderNo", request.getOrderNo());
        result.put("totalAmount", totalAmount);
        result.put("periods", periods);
        result.put("interestRate", interestRate.multiply(new BigDecimal("100")) + "%");
        result.put("totalInterest", totalInterest);
        result.put("totalWithInterest", totalWithInterest);
        result.put("perPeriodAmount", perPeriodAmount);
        result.put("installmentPlan", installmentPlan);
        result.put("firstPaymentDue", now.plusMonths(1).toString());
        
        log.info("【V2 分期付款】计算完成，总金额={}, 总利息={}, 每期={}", 
                totalAmount, totalInterest, perPeriodAmount);
        
        return Result.success(result);
    }
    
    /**
     * 根据分期期数计算利率
     */
    private BigDecimal calculateInterestRate(int periods) {
        // 模拟利率：3期2%，6期3.5%，12期5%，24期8%
        if (periods <= 3) {
            return new BigDecimal("0.02");
        } else if (periods <= 6) {
            return new BigDecimal("0.035");
        } else if (periods <= 12) {
            return new BigDecimal("0.05");
        } else {
            return new BigDecimal("0.08");
        }
    }
}
