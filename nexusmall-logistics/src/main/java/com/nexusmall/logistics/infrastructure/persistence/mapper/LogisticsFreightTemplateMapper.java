package com.nexusmall.logistics.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运费模板 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface LogisticsFreightTemplateMapper extends BaseMapper<LogisticsFreightTemplate> {
}
