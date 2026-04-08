package com.nexusmall.member.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.member.domain.entity.GrowthChangeHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成长值变化历史 Mapper
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface GrowthChangeHistoryMapper extends BaseMapper<GrowthChangeHistory> {
}
