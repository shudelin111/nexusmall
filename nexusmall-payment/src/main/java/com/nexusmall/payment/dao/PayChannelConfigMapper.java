package com.nexusmall.payment.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.payment.entity.PayChannelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付渠道配置 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface PayChannelConfigMapper extends BaseMapper<PayChannelConfig> {

}
