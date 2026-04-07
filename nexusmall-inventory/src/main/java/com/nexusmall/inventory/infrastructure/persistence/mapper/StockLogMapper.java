package com.nexusmall.inventory.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.inventory.domain.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}
