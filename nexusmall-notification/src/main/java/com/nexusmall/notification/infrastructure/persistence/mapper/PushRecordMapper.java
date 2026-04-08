package com.nexusmall.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.notification.domain.entity.PushRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送通知记录 Mapper
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface PushRecordMapper extends BaseMapper<PushRecord> {
}
