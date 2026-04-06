package com.nexusmall.common.constant;

/**
 * API 版本常量
 * <p>
 * 定义系统中所有 API 的版本号
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public class ApiVersionConstants {

    /**
     * 当前 API 版本（默认版本）
     */
    public static final String CURRENT_VERSION = "v1";

    /**
     * Header 名称：API 版本
     */
    public static final String HEADER_API_VERSION = "X-API-Version";

    /**
     * 支持的 API 版本列表
     */
    public static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};

    /**
     * 私有构造函数，防止实例化
     */
    private ApiVersionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
