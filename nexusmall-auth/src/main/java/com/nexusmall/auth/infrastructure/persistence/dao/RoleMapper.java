package com.nexusmall.auth.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.domain.entity.Role;
import com.nexusmall.auth.domain.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> findByUserId(Long userId);

    Role selectById(Long id);

    Role findByRoleCode(String roleCode);

    int insert(Role role);

    int updateById(Role role);

    int deleteById(Long id);
    
    /**
     * 插入角色权限关联
     */
    int insertRolePermission(RolePermission rolePermission);
    
    /**
     * 删除角色权限关联
     */
    int deleteRolePermissions(@Param("roleId") Long roleId);
}
