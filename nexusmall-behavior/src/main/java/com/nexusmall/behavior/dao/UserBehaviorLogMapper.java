package com.nexusmall.behavior.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.behavior.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为日志 Mapper 接口
 * 
 * @author shudl
 * @since 2026-03-25
 */
@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

}
