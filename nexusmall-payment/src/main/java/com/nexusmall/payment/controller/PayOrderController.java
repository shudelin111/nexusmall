package com.nexusmall.payment.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.payment.service.PayOrderService;
import com.nexusmall.payment.vo.request.CreatePayOrderRequest;
import com.nexusmall.payment.vo.response.PayOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付单控制器
 * <p>
 * 提供支付单的创建、查询、回调等REST API
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/payments")  // RESTful 资源命名：支付单使用 payments
@RequiredArgsConstructor
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
@Tag(name = "支付单管理", description = "支付单的创建、查询、回调等接口")
public class PayOrderController {

    private final PayOrderService payOrderService;

    /**
     * 创建支付单 V1
     *
     * @param request 创建支付单请求
     * @return 支付表单HTML或二维码URL
     */
    @PostMapping(value = "/create", headers = "X-API-Version=v1")
    @Operation(summary = "创建支付单 V1", description = "V1版本：基础支付功能")
    public Result<String> createPayOrderV1(@RequestBody CreatePayOrderRequest request) {
        log.info("【V1 创建支付单】orderNo={}, channel={}", request.getOrderNo(), request.getChannelCode());
        
        String paymentResult = payOrderService.createPayOrder(request);
        return Result.success(paymentResult);
    }

    /**
     * 查询支付单 V1
     *
     * @param paymentNo 支付单号
     * @return 支付单详情
     */
    @GetMapping(value = "/query/{paymentNo}", headers = "X-API-Version=v1")
    @Operation(summary = "查询支付单 V1", description = "V1版本：基础查询功能")
    public Result<PayOrderResponse> queryPayOrderV1(
            @Parameter(description = "支付单号", required = true)
            @PathVariable String paymentNo) {
        log.info("【V1 查询支付单】paymentNo={}", paymentNo);
        
        PayOrderResponse response = payOrderService.queryPayOrder(paymentNo);
        return Result.success(response);
    }

    /**
     * 查询支付单（通过订单号）
     *
     * @param orderNo 订单号
     * @return 支付单详情
     */
    @GetMapping("/query-by-order/{orderNo}")
    @Operation(summary = "根据订单号查询支付单", description = "根据订单号查询最新的支付单")
    public Result<PayOrderResponse> queryByOrderNo(
            @Parameter(description = "订单号", required = true)
            @PathVariable String orderNo) {
        log.info("【查询支付单】orderNo={}", orderNo);
        
        PayOrderResponse response = payOrderService.queryByOrderNo(orderNo);
        return Result.success(response);
    }

    /**
     * 支付宝支付回调
     *
     * @param callbackData 回调数据
     * @return 处理结果
     */
    @PostMapping("/callback/alipay")
    @Operation(summary = "支付宝支付回调", description = "接收支付宝异步通知")
    public String alipayCallback(@RequestBody Object callbackData) {
        log.info("【支付宝回调】收到回调通知");
        
        boolean success = payOrderService.handleCallback("ALIPAY", callbackData);
        return success ? "success" : "fail";
    }

    /**
     * 微信支付回调
     *
     * @param callbackData 回调数据
     * @return 处理结果
     */
    @PostMapping("/callback/wechat")
    @Operation(summary = "微信支付回调", description = "接收微信支付异步通知")
    public String wechatCallback(@RequestBody Object callbackData) {
        log.info("【微信回调】收到回调通知");
        
        boolean success = payOrderService.handleCallback("WECHAT", callbackData);
        return success ? "success" : "fail";
    }

    /**
     * 手动关闭过期订单（测试用）
     *
     * @return 关闭数量
     */
    @PostMapping("/close-expired")
    @Operation(summary = "关闭过期订单", description = "手动触发关闭过期支付单任务")
    public Result<Integer> closeExpiredOrders() {
        log.info("【关闭过期订单】手动触发");
        
        int count = payOrderService.closeExpiredOrders();
        return Result.success(count);
    }
}
