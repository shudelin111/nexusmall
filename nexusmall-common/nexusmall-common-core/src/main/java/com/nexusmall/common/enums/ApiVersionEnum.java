package com.nexusmall.common.enums;

import lombok.Getter;

/**
 * API 版本枚举
 * <p>
 * 业界标准：
 * - 使用枚举管理版本号，避免魔法字符串
 * - 定义版本兼容性规则
 * - 支持版本优先级排序
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Getter
public enum ApiVersionEnum {

    /**
     * V1 版本（初始版本）
     */
    V1("v1", 1, true),

    /**
     * V2 版本（当前最新版本）
     */
    V2("v2", 2, false);

    /**
     * 版本号字符串
     */
    private final String version;

    /**
     * 版本优先级（数字越大优先级越高）
     */
    private final int priority;

    /**
     * 是否已废弃
     */
    private final boolean deprecated;

    ApiVersionEnum(String version, int priority, boolean deprecated) {
        this.version = version;
        this.priority = priority;
        this.deprecated = deprecated;
    }

    /**
     * 根据版本号字符串获取枚举
     *
     * @param version 版本号（如 "v1"）
     * @return 对应的枚举，未找到返回 null
     */
    public static ApiVersionEnum fromString(String version) {
        if (version == null) {
            return null;
        }
        for (ApiVersionEnum v : values()) {
            if (v.getVersion().equalsIgnoreCase(version)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 检查版本是否有效
     *
     * @param version 版本号
     * @return true=有效
     */
    public static boolean isValid(String version) {
        return fromString(version) != null;
    }

    /**
     * 获取最新版本
     *
     * @return 最新版本枚举
     */
    public static ApiVersionEnum getLatest() {
        ApiVersionEnum latest = V1;
        for (ApiVersionEnum v : values()) {
            if (v.getPriority() > latest.getPriority()) {
                latest = v;
            }
        }
        return latest;
    }

    /**
     * 检查当前版本是否高于或等于指定版本
     *
     * @param other 对比的版本
     * @return true=当前版本 >= 指定版本
     */
    public boolean isGreaterOrEqual(ApiVersionEnum other) {
        return this.priority >= other.priority;
    }

    /**
     * 检查当前版本是否已废弃
     *
     * @return true=已废弃
     */
    public boolean isDeprecated() {
        return this.deprecated;
    }
}
