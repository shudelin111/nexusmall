package com.nexusmall.common.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用户上下文工具类
 * <p>
 * 业界标准：
 * - 网关统一验证 JWT 后，将用户信息通过 Header 透传
 * - 下游微服务从 Header 读取用户信息，不再重复验证 Token
 * - ThreadLocal 存储当前请求的用户信息
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public class UserContext {

    /**
     * Header 名称常量
     */
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回 null
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HEADER_USER_NAME);
    }

    /**
     * 获取当前用户角色列表
     *
     * @return 角色列表，未登录返回空列表
     */
    public static List<String> getCurrentRoles() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Collections.emptyList();
        }
        
        String rolesHeader = request.getHeader(HEADER_USER_ROLES);
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.asList(rolesHeader.split(","));
    }

    /**
     * 获取当前用户权限列表
     *
     * @return 权限列表，未登录返回空列表
     */
    public static List<String> getCurrentPermissions() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Collections.emptyList();
        }
        
        String permissionsHeader = request.getHeader(HEADER_USER_PERMISSIONS);
        if (permissionsHeader == null || permissionsHeader.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.asList(permissionsHeader.split(","));
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true=已登录
     */
    public static boolean isAuthenticated() {
        return getCurrentUsername() != null;
    }

    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色代码
     * @return true=有该角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles.contains(role);
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限代码
     * @return true=有该权限
     */
    public static boolean hasPermission(String permission) {
        List<String> permissions = getCurrentPermissions();
        return permissions.contains(permission);
    }

    /**
     * 获取当前 HTTP 请求
     *
     * @return HttpServletRequest
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
