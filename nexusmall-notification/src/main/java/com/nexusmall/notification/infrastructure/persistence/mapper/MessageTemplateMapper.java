package com.nexusmall.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.notification.domain.entity.MessageTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息模板 Mapper
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {

    /**
     * 根据模板代码和渠道查询模板
     *
     * @param templateCode 模板代码
     * @param channel      渠道
     * @return 消息模板
     */
    MessageTemplate selectByCodeAndChannel(@Param("templateCode") String templateCode,
                                           @Param("channel") Integer channel);
}
