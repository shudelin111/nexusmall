package com.nexusmall.logistics.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;

import java.math.BigDecimal;

/**
 * 运费计算服务接口
 * <p>
 * 业界标准?
 * - 支持多种计费方式
 * - 支持包邮规则
 * - 支持首重续重计费模型
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsFreightTemplateService extends IService<LogisticsFreightTemplate> {

    /**
     * 计算运费
     * <p>
     * 业界标准?
     * - 按重量：首重费用 + (总重?- 首重) / 续重 * 续重费用
     * - 按体积：类似按重量计?
     * - 按件数：件数 * 单价
     * - 如果订单金额 >= 包邮门槛，则运费?
     * </p>
     *
     * @param templateId  运费模板ID
     * @param weight      重量（kg?
     * @param volume      体积（m³?
     * @param pieceCount  件数
     * @param orderAmount 订单金额（用于判断是否包邮）
     * @return 运费
     */
    BigDecimal calculateFreight(Long templateId, BigDecimal weight, BigDecimal volume,
                                 Integer pieceCount, BigDecimal orderAmount);

    /**
     * 获取默认运费模板
     *
     * @return 默认运费模板
     */
    LogisticsFreightTemplate getDefaultTemplate();
}
