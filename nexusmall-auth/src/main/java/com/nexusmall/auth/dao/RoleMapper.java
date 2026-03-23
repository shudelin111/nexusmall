package com.nexusmall.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> findByUserId(Long userId);

    Role selectById(Long id);

    Role findByRoleCode(String roleCode);

    int insert(Role role);

    int updateById(Role role);

    int deleteById(Long id);
}
