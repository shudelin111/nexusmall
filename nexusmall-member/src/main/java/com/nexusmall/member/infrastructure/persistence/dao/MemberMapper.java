package com.nexusmall.member.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.member.domain.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper
 *
 * @author shudl
 * @since 2026-04-06
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
