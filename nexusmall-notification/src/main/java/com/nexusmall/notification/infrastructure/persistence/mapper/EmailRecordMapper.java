package com.nexusmall.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.notification.domain.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送记录 Mapper
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface EmailRecordMapper extends BaseMapper<EmailRecord> {
}
