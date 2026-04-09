package com.nexusmall.logistics.infrastructure.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.logistics.domain.calculator.FreightCalculator;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsFreightTemplateMapper;
import com.nexusmall.logistics.application.service.LogisticsCacheService;
import com.nexusmall.logistics.application.service.LogisticsFreightTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 运费计算服务实现类
 * <p>
 * 业界标准计费模型：
 * - 按重量：首重费用 + ceil((总重量- 首重) / 续重) * 续重费用
 * - 按体积：类似按重量
 * - 按件数：件数 * 单价（首重费用作为单价）
 * - 包邮规则：订单金额>= 包邮门槛时，运费用
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsFreightTemplateServiceImpl extends ServiceImpl<LogisticsFreightTemplateMapper, LogisticsFreightTemplate> implements LogisticsFreightTemplateService {

    private final LogisticsCacheService cacheService;
    private final FreightCalculator freightCalculator = new FreightCalculator();

    @Override
    public BigDecimal calculateFreight(Long templateId, BigDecimal weight, BigDecimal volume,
                                        Integer pieceCount, BigDecimal orderAmount) {
        log.info("【计算运费】templateId={}, weight={}, volume={}, pieceCount={}, orderAmount={}",
                templateId, weight, volume, pieceCount, orderAmount);

        // 1. 获取运费模板（带缓存：
        LogisticsFreightTemplate template = cacheService.getFreightTemplateWithCache(templateId);
        
        // 2. 使用领域计算器计算运费
        BigDecimal freight = freightCalculator.calculate(
                template, weight, volume, pieceCount, orderAmount
        );

        log.info("【计算运费完成】freight={}", freight);
        return freight;
    }

    @Override
    public LogisticsFreightTemplate getDefaultTemplate() {
        // 使用缓存查询默认模板
        return cacheService.getDefaultFreightTemplateWithCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(LogisticsFreightTemplate entity) {
        boolean success = super.save(entity);
        if (success) {
            // 清除缓存
            cacheService.evictAllFreightTemplateCache();
            log.info("【新增运费模板】已清除缓存，templateId={}", entity.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(LogisticsFreightTemplate entity) {
        boolean success = super.updateById(entity);
        if (success) {
            // 清除缓存
            cacheService.evictFreightTemplateCache(entity.getId());
            log.info("【更新运费模板】已清除缓存，templateId={}", entity.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        boolean success = super.removeById(id);
        if (success) {
            // 清除缓存
            cacheService.evictFreightTemplateCache((Long) id);
            log.info("【删除运费模板】已清除缓存，templateId={}", id);
        }
        return success;
    }
}
