package com.nexusmall.payment.adapter.outbound.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.payment.domain.model.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付�?Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
