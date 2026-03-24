package com.nexusmall.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.entity.User;
import com.nexusmall.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User findByUsername(String username);

    User selectById(Long id);

    int insert(User user);

    int updateById(User user);

    int deleteById(Long id);
    
    /**
     * 插入用户角色关联
     */
    int insertUserRole(UserRole userRole);
    
    /**
     * 删除用户角色关联
     */
    int deleteUserRoles(@Param("userId") Long userId);
}
