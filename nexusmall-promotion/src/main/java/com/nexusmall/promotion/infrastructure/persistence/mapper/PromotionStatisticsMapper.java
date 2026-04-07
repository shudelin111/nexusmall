package com.nexusmall.promotion.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.promotion.domain.entity.PromotionStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 营销数据统计 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface PromotionStatisticsMapper extends BaseMapper<PromotionStatistics> {
}
