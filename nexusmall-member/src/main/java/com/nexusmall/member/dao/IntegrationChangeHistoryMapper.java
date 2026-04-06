package com.nexusmall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.member.entity.IntegrationChangeHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变化历史 Mapper
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface IntegrationChangeHistoryMapper extends BaseMapper<IntegrationChangeHistory> {
}
