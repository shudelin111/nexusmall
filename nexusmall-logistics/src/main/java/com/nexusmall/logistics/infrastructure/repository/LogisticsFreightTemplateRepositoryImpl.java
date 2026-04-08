package com.nexusmall.logistics.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.logistics.domain.repository.LogisticsFreightTemplateRepository;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsFreightTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 运费模板仓储实现（基础设施层）
 *
 * @author shudl
 * @since 2026-04-07
 */
@Repository
@RequiredArgsConstructor
public class LogisticsFreightTemplateRepositoryImpl implements LogisticsFreightTemplateRepository {

    private final LogisticsFreightTemplateMapper logisticsFreightTemplateMapper;

    @Override
    public LogisticsFreightTemplate findById(Long id) {
        return logisticsFreightTemplateMapper.selectById(id);
    }

    @Override
    public LogisticsFreightTemplate findDefault() {
        LambdaQueryWrapper<LogisticsFreightTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsFreightTemplate::getIsDefault, 1);
        wrapper.last("LIMIT 1");
        return logisticsFreightTemplateMapper.selectOne(wrapper);
    }

    @Override
    public boolean save(LogisticsFreightTemplate template) {
        return logisticsFreightTemplateMapper.insert(template) > 0;
    }

    @Override
    public boolean update(LogisticsFreightTemplate template) {
        return logisticsFreightTemplateMapper.updateById(template) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return logisticsFreightTemplateMapper.deleteById(id) > 0;
    }
}
