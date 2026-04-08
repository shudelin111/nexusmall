package com.nexusmall.auth.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> findByUserId(Long userId);

    Permission selectById(Long id);

    Permission findByPermissionCode(String permissionCode);

    List<Permission> findAllEnabled();

    int insert(Permission permission);

    int updateById(Permission permission);

    int deleteById(Long id);
}
