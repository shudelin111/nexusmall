package com.nexusmall.logistics.domain.repository;

import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;

/**
 * 运费模板仓储接口（领域层）
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsFreightTemplateRepository {

    /**
     * 根据ID查询
     */
    LogisticsFreightTemplate findById(Long id);

    /**
     * 查询默认运费模板
     */
    LogisticsFreightTemplate findDefault();

    /**
     * 保存运费模板
     */
    boolean save(LogisticsFreightTemplate template);

    /**
     * 更新运费模板
     */
    boolean update(LogisticsFreightTemplate template);

    /**
     * 根据ID删除
     */
    boolean deleteById(Long id);
}
