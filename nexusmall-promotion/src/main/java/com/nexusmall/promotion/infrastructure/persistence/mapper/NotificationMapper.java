package com.nexusmall.promotion.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.promotion.domain.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
