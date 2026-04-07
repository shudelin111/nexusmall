package com.nexusmall.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息 Mapper
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessage> {
}
