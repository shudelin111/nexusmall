package com.nexusmall.logistics.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.logistics.application.service.LogisticsApplicationService;
import com.nexusmall.logistics.interfaces.dto.CalculateFreightRequest;
import com.nexusmall.logistics.interfaces.dto.FreightResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 运费计算 Controller
 * <p>
 * 业界标准：
 * - 支持多种计费方式
 * - 实时计算运费
 * - 返回详细计费说明
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/freight")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "运费计算", description = "运费计算、运费模板查询")
public class FreightController {

    private final LogisticsApplicationService logisticsApplicationService;

    /**
     * 计算运费
     *
     * @param request 计算请求
     * @return 运费结果
     */
    @PostMapping(value = "/calculate", headers = "X-API-Version=v1")
    @Operation(summary = "计算运费", description = "根据重量/体积/件数计算运费，支持包邮规则")
    public Result<FreightResultVO> calculateFreight(@Validated @RequestBody CalculateFreightRequest request) {
        log.info("【计算运费】weight={}, volume={}, pieceCount={}, orderAmount={}",
                request.getWeight(), request.getVolume(), request.getPieceCount(), request.getOrderAmount());

        // 调用应用服务
        FreightResultVO result = logisticsApplicationService.calculateFreight(request);

        log.info("【计算运费完成】freight={}, isFreeShipping={}", 
                result.getFreightAmount(), result.getIsFreeShipping());
        return Result.success(result);
    }
}
