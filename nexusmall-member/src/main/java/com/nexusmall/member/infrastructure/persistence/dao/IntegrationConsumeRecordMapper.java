package com.nexusmall.member.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.member.domain.entity.IntegrationConsumeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分兑换记录 Mapper
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface IntegrationConsumeRecordMapper extends BaseMapper<IntegrationConsumeRecord> {
}
