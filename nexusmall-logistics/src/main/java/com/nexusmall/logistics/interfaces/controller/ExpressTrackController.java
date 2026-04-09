package com.nexusmall.logistics.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;
import com.nexusmall.logistics.application.service.ExpressTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 第三方物流轨迹查?Controller
 * <p>
 * 业界标准：
 * - 支持快递鸟、快递100等主流服务商
 * - 实时查询物流轨迹
 * - 支持订阅/取消订阅
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/express-tracks")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "第三方物流轨迹", description = "查询、订阅快递轨迹（快递鸟/快递100）")
public class ExpressTrackController {

    private final ExpressTrackService expressTrackService;

    /**
     * 查询物流轨迹
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @return 物流轨迹列表
     */
    @GetMapping(value = "/query", headers = "X-API-Version=v1")
    @Operation(summary = "查询物流轨迹", description = "从第三方API实时查询物流轨迹，支持快递鸟和快递100")
    public Result<List<LogisticsTrack>> queryExpressTrack(
            @Parameter(description = "快递公司名称", required = true, example = "顺丰速运")
            @RequestParam String expressCompany,
            @Parameter(description = "快递单号", required = true, example = "SF20260407000001")
            @RequestParam String expressNo) {
        log.info("【查询物流轨迹】expressCompany={}, expressNo={}", expressCompany, expressNo);
        
        List<LogisticsTrack> tracks = expressTrackService.queryExpressTrack(expressCompany, expressNo);
        
        if (tracks == null || tracks.isEmpty()) {
            return Result.failure("404", "未查询到物流轨迹信息");
        }
        
        return Result.success(tracks);
    }

    /**
     * 订阅物流轨迹
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @param callbackUrl    回调地址
     * @return 是否订阅成功
     */
    @PostMapping(value = "/subscribe", headers = "X-API-Version=v1")
    @Operation(summary = "订阅物流轨迹", description = "订阅后，第三方会在状态变更时主动推送通知")
    public Result<Void> subscribeExpressTrack(
            @Parameter(description = "快递公司名称", required = true, example = "顺丰速运")
            @RequestParam String expressCompany,
            @Parameter(description = "快递单号", required = true, example = "SF20260407000001")
            @RequestParam String expressNo,
            @Parameter(description = "回调地址", required = true, example = "https://api.nexusmall.com/logistics/callback")
            @RequestParam String callbackUrl) {
        log.info("【订阅物流轨迹】expressCompany={}, expressNo={}, callbackUrl={}", 
                expressCompany, expressNo, callbackUrl);
        
        boolean success = expressTrackService.subscribeExpressTrack(expressCompany, expressNo, callbackUrl);
        
        return success ? Result.success() : Result.failure("500", "订阅失败");
    }

    /**
     * 取消订阅物流轨迹
     *
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     * @return 是否取消成功
     */
    @PostMapping(value = "/unsubscribe", headers = "X-API-Version=v1")
    @Operation(summary = "取消订阅物流轨迹", description = "取消之前订阅的物流轨迹推送")
    public Result<Void> unsubscribeExpressTrack(
            @Parameter(description = "快递公司名称", required = true, example = "顺丰速运")
            @RequestParam String expressCompany,
            @Parameter(description = "快递单号", required = true, example = "SF20260407000001")
            @RequestParam String expressNo) {
        log.info("【取消订阅物流轨迹】expressCompany={}, expressNo={}", expressCompany, expressNo);
        
        boolean success = expressTrackService.unsubscribeExpressTrack(expressCompany, expressNo);
        
        return success ? Result.success() : Result.failure("500", "取消订阅失败");
    }
}
