package com.nexusmall.logistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 物流缓存配置
 * <p>
 * 业界标准?
 * - 缓存仓库信息（变化频率低?
 * - 缓存运费模板（变化频率低?
 * - 设置合理的过期时?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "logistics.cache")
public class LogisticsCacheConfig {

    /**
     * 是否启用缓存
     */
    private boolean enabled = true;

    /**
     * 仓库信息缓存配置
     */
    private Warehouse warehouse = new Warehouse();

    /**
     * 运费模板缓存配置
     */
    private FreightTemplate freightTemplate = new FreightTemplate();

    /**
     * 仓库缓存配置
     */
    @Data
    public static class Warehouse {
        /**
         * 缓存前缀
         */
        private String keyPrefix = "logistics:warehouse:";

        /**
         * 单个仓库缓存TTL（秒），默认1小时
         */
        private long ttlSeconds = 3600;

        /**
         * 仓库列表缓存TTL（秒），默认30分钟
         */
        private long listTtlSeconds = 1800;

        /**
         * 最大缓存数?
         */
        private int maxSize = 100;
    }

    /**
     * 运费模板缓存配置
     */
    @Data
    public static class FreightTemplate {
        /**
         * 缓存前缀
         */
        private String keyPrefix = "logistics:freight:template:";

        /**
         * 缓存TTL（秒），默认1小时
         */
        private long ttlSeconds = 3600;

        /**
         * 默认模板缓存TTL（秒），默认2小时（访问频率高?
         */
        private long defaultTtlSeconds = 7200;

        /**
         * 最大缓存数?
         */
        private int maxSize = 50;
    }
}
