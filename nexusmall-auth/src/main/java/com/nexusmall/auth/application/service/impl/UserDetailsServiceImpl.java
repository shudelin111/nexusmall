package com.nexusmall.auth.application.service.impl;

import com.nexusmall.auth.infrastructure.persistence.dao.PermissionMapper;
import com.nexusmall.auth.infrastructure.persistence.dao.RoleMapper;
import com.nexusmall.auth.infrastructure.persistence.dao.UserMapper;
import com.nexusmall.auth.domain.entity.Permission;
import com.nexusmall.auth.domain.entity.Role;
import com.nexusmall.auth.domain.entity.User;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(ErrorMessageConstants.Auth.USER_NOT_FOUND_WITH_USERNAME + username);
        }
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException(ErrorMessageConstants.Auth.USER_DISABLED_WITH_USERNAME + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限
        List<Role> roles = roleMapper.findByUserId(user.getId());
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
        }

        // 添加具体权限
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
