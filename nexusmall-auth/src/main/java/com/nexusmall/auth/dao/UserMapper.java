package com.nexusmall.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User findByUsername(String username);

    User selectById(Long id);

    int insert(User user);

    int updateById(User user);

    int deleteById(Long id);
}
