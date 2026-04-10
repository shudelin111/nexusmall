package com.nexusmall.logistics.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;
import com.nexusmall.logistics.application.service.LogisticsOrderService;
import com.nexusmall.logistics.application.service.LogisticsTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物流订单 Controller
 * <p>
 * 业界标准：
 * - RESTful API设计
 * - 支持版本控制
 * - 完整的物流查询接口
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/logistics-orders")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "物流订单管理", description = "物流订单查询、轨迹跟踪")
public class LogisticsOrderController {

    private final LogisticsOrderService logisticsOrderService;
    private final LogisticsTrackService logisticsTrackService;

    /**
     * 根据订单编号查询物流信息
     *
     * @param orderSn 订单编号
     * @return 物流订单及轨迹
     */
    @GetMapping(value = "/order/{orderSn}", headers = "X-API-Version=v1")
    @Operation(summary = "根据订单编号查询物流", description = "查询指定订单的物流信息及轨迹")
    public Result<LogisticsOrder> getByOrderSn(
            @Parameter(description = "订单编号", required = true)
            @PathVariable String orderSn) {
        log.info("【查询物流订单】orderSn={}", orderSn);
        LogisticsOrder order = logisticsOrderService.getByOrderSn(orderSn);
        if (order == null) {
            return Result.failure("404", "物流订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 根据快递单号查询物流信息
     *
     * @param expressNo 快递单号
     * @return 物流订单及轨迹
     */
    @GetMapping(value = "/express/{expressNo}", headers = "X-API-Version=v1")
    @Operation(summary = "根据快递单号查询物流", description = "查询指定快递单号的物流信息及轨迹")
    public Result<LogisticsOrder> getByExpressNo(
            @Parameter(description = "快递单号", required = true)
            @PathVariable String expressNo) {
        log.info("【查询物流订单】expressNo={}", expressNo);
        LogisticsOrder order = logisticsOrderService.getByExpressNo(expressNo);
        if (order == null) {
            return Result.failure("404", "物流订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 查询物流轨迹列表
     *
     * @param orderId 物流订单ID
     * @return 轨迹列表（按时间倒序）
     */
    @GetMapping(value = "/{orderId}/tracks", headers = "X-API-Version=v1")
    @Operation(summary = "查询物流轨迹", description = "查询指定物流订单的完整轨迹链条")
    public Result<List<LogisticsTrack>> listTracks(
            @Parameter(description = "物流订单ID", required = true)
            @PathVariable Long orderId) {
        log.info("【查询物流轨迹】orderId={}", orderId);
        List<LogisticsTrack> tracks = logisticsTrackService.listByLogisticsOrderId(orderId);
        return Result.success(tracks);
    }

    /**
     * 确认签收
     *
     * @param id 物流订单ID
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/receive", headers = "X-API-Version=v1")
    @Operation(summary = "确认签收", description = "用户确认收到货物")
    public Result<Void> confirmReceive(
            @Parameter(description = "物流订单ID", required = true)
            @PathVariable Long id) {
        log.info("【确认签收】id={}", id);
        boolean success = logisticsOrderService.confirmReceive(id);
        return success ? Result.success() : Result.failure("500", "确认签收失败");
    }
}
