package com.nexusmall.logistics.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库 Mapper 接口
 *
 * @author shudl
 * @since 2026-04-07
 */
@Mapper
public interface LogisticsWarehouseMapper extends BaseMapper<LogisticsWarehouse> {
}
