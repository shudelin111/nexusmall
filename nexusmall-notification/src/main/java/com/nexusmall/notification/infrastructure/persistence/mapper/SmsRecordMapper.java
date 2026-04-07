package com.nexusmall.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.notification.domain.entity.SmsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信发送记录 Mapper
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface SmsRecordMapper extends BaseMapper<SmsRecord> {
}
