package com.nexusmall.logistics.domain.calculator;

import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.domain.enums.ChargeTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 运费计算器（领域服务?
 * <p>
 * 业界标准?
 * - 纯业务逻辑，无外部依赖
 * - 支持多种计费策略（按重量/体积/件数?
 * - 包邮规则判断
 * - 可在单元测试中独立测?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
public class FreightCalculator {

    /**
     * 计算运费
     * <p>
     * 业界标准计费模型?
     * - 按重量：首重费用 + ceil((总重?- 首重) / 续重) * 续重费用
     * - 按体积：类似按重?
     * - 按件数：件数 * 单价
     * - 包邮规则：订单金?>= 包邮门槛时，运费?
     * </p>
     *
     * @param template   运费模板
     * @param weight     重量（kg?
     * @param volume     体积（m³?
     * @param pieceCount 件数
     * @param orderAmount 订单金额
     * @return 运费
     */
    public BigDecimal calculate(LogisticsFreightTemplate template, 
                                BigDecimal weight, 
                                BigDecimal volume,
                                Integer pieceCount, 
                                BigDecimal orderAmount) {
        if (template == null) {
            log.warn("【运费计算】运费模板为空，返回默认运费");
            return BigDecimal.TEN;
        }

        // 1. 检查是否满足包邮条?
        if (isFreeShipping(template, orderAmount)) {
            log.info("【运费计算】满足包邮条件，orderAmount={}, freeThreshold={}", 
                    orderAmount, template.getFreeThreshold());
            return BigDecimal.ZERO;
        }

        // 2. 根据计费方式计算运费
        ChargeTypeEnum chargeType = ChargeTypeEnum.getByCode(template.getChargeType());
        if (chargeType == null) {
            log.error("【运费计算】未知的计费方式，chargeType={}", template.getChargeType());
            return BigDecimal.TEN;
        }

        switch (chargeType) {
            case BY_WEIGHT:
                return calculateByWeight(template, weight);
            case BY_VOLUME:
                return calculateByVolume(template, volume);
            case BY_PIECE:
                return calculateByPiece(template, pieceCount);
            default:
                return BigDecimal.TEN;
        }
    }

    /**
     * 判断是否包邮
     */
    private boolean isFreeShipping(LogisticsFreightTemplate template, BigDecimal orderAmount) {
        return template.getFreeThreshold() != null 
                && orderAmount != null 
                && orderAmount.compareTo(template.getFreeThreshold()) >= 0;
    }

    /**
     * 按重量计算运?
     * <p>
     * 公式：首重费?+ ceil((总重?- 首重) / 续重) * 续重费用
     * 如果总重?<= 首重，则只收首重费用
     * </p>
     */
    private BigDecimal calculateByWeight(LogisticsFreightTemplate template, BigDecimal weight) {
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            return template.getFirstFee();
        }

        // 如果重量不超过首重，只收首重费用
        if (weight.compareTo(template.getFirstWeight()) <= 0) {
            return template.getFirstFee();
        }

        // 计算续重部分
        BigDecimal remainingWeight = weight.subtract(template.getFirstWeight());
        BigDecimal continuedUnits = remainingWeight.divide(
                template.getContinuedWeight(), 0, RoundingMode.CEILING);
        
        return template.getFirstFee().add(continuedUnits.multiply(template.getContinuedFee()));
    }

    /**
     * 按体积计算运?
     * <p>
     * 公式：类似按重量计算
     * </p>
     */
    private BigDecimal calculateByVolume(LogisticsFreightTemplate template, BigDecimal volume) {
        if (volume == null || volume.compareTo(BigDecimal.ZERO) <= 0) {
            return template.getFirstFee();
        }

        // 如果体积不超过首体积，只收首重费?
        if (volume.compareTo(template.getFirstWeight()) <= 0) {
            return template.getFirstFee();
        }

        // 计算续体积部?
        BigDecimal remainingVolume = volume.subtract(template.getFirstWeight());
        BigDecimal continuedUnits = remainingVolume.divide(
                template.getContinuedWeight(), 0, RoundingMode.CEILING);
        
        return template.getFirstFee().add(continuedUnits.multiply(template.getContinuedFee()));
    }

    /**
     * 按件数计算运?
     * <p>
     * 公式：件?* 首重费用（作为单价）
     * </p>
     */
    private BigDecimal calculateByPiece(LogisticsFreightTemplate template, Integer pieceCount) {
        if (pieceCount == null || pieceCount <= 0) {
            return template.getFirstFee();
        }

        return template.getFirstFee().multiply(new BigDecimal(pieceCount));
    }
}
