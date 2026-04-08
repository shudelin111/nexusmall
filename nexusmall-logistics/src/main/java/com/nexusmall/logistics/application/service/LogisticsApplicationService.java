package com.nexusmall.logistics.application.service;

import com.nexusmall.logistics.domain.calculator.FreightCalculator;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.application.service.LogisticsCacheService;
import com.nexusmall.logistics.application.service.LogisticsFreightTemplateService;
import com.nexusmall.logistics.interfaces.dto.CalculateFreightRequest;
import com.nexusmall.logistics.interfaces.dto.FreightResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 物流应用服务
 * <p>
 * 业界标准：
 * - 编排领域服务
 * - DTO/VO转换
 * - 事务边界控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsApplicationService {

    private final LogisticsFreightTemplateService freightTemplateService;
    private final LogisticsCacheService cacheService;
    private final FreightCalculator freightCalculator = new FreightCalculator();

    /**
     * 计算运费（应用服务）
     *
     * @param request 计算请求
     * @return 运费结果
     */
    public FreightResultVO calculateFreight(CalculateFreightRequest request) {
        log.info("【应用服务-计算运费】weight={}, volume={}, pieceCount={}, orderAmount={}",
                request.getWeight(), request.getVolume(), request.getPieceCount(), request.getOrderAmount());

        // 1. 获取运费模板
        Long templateId = request.getTemplateId();
        if (templateId == null) {
            // 使用默认模板
            LogisticsFreightTemplate defaultTemplate = cacheService.getDefaultFreightTemplateWithCache();
            if (defaultTemplate != null) {
                templateId = defaultTemplate.getId();
            } else {
                throw new RuntimeException("没有可用的运费模板");
            }
        }

        // 2. 获取运费模板详情
        LogisticsFreightTemplate template = cacheService.getFreightTemplateWithCache(templateId);
        if (template == null) {
            throw new RuntimeException("运费模板不存在，templateId=" + templateId);
        }

        // 3. 调用领域计算器计算运费
        BigDecimal freight = freightCalculator.calculate(
                template,
                request.getWeight(),
                request.getVolume(),
                request.getPieceCount(),
                request.getOrderAmount()
        );

        // 4. 构建响应VO
        boolean isFreeShipping = freight.compareTo(BigDecimal.ZERO) == 0;
        String description = buildFreightDescription(template, request, freight);

        return FreightResultVO.builder()
                .freightAmount(freight)
                .isFreeShipping(isFreeShipping)
                .chargeType(template.getChargeType())
                .description(description)
                .templateId(templateId)
                .templateName(template.getTemplateName())
                .build();
    }

    /**
     * 生成计费说明
     */
    private String buildFreightDescription(LogisticsFreightTemplate template, 
                                          CalculateFreightRequest request, 
                                          BigDecimal freight) {
        if (freight.compareTo(BigDecimal.ZERO) == 0) {
            return String.format("订单金额%.2f元，满足包邮条件（满%.2f元包邮），运费0元",
                    request.getOrderAmount(), template.getFreeThreshold());
        }

        switch (template.getChargeType()) {
            case 1: // 按重量
                return String.format("按重量计费：首重%.2fkg费用%.2f元，续重每%.2fkg费用%.2f元，总重量%.2fkg，运费%.2f元",
                        template.getFirstWeight(), template.getFirstFee(),
                        template.getContinuedWeight(), template.getContinuedFee(),
                        request.getWeight(), freight);
            case 2: // 按体积
                return String.format("按体积计费：首体积%.2fm³费用%.2f元，续体积每%.2fm³费用%.2f元，总体积%.2fm³，运费%.2f元",
                        template.getFirstWeight(), template.getFirstFee(),
                        template.getContinuedWeight(), template.getContinuedFee(),
                        request.getVolume(), freight);
            case 3: // 按件数
                return String.format("按件数计费：每件%.2f元，共%d件，运费%.2f元",
                        template.getFirstFee(), request.getPieceCount(), freight);
            default:
                return "运费：" + freight + "元";
        }
    }
}
