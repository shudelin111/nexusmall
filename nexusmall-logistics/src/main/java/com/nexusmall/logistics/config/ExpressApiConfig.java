package com.nexusmall.logistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 第三方物流API配置
 * <p>
 * 业界标准：
 * - 支持多家快递公司API对接
 * - 配置化API密钥和端?
 * - 便于切换物流服务?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "logistics.express")
public class ExpressApiConfig {

    /**
     * 快递鸟API配置
     */
    private KuaiDiNiao kuaidiniao = new KuaiDiNiao();

    /**
     * 快?00 API配置
     */
    private KuaiDi100 kuaidi100 = new KuaiDi100();

    /**
     * 默认使用的物流查询服务商
     */
    private String defaultProvider = "kuaidiniao";

    /**
     * 快递鸟API配置
     */
    @Data
    public static class KuaiDiNiao {
        /**
         * 商户ID
         */
        private String businessId;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiUrl = "http://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

        /**
         * 是否启用
         */
        private boolean enabled = true;
    }

    /**
     * 快?00 API配置
     */
    @Data
    public static class KuaiDi100 {
        /**
         * 客户ID
         */
        private String customerId;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiUrl = "https://poll.kuaidi100.com/poll/query.do";

        /**
         * 是否启用
         */
        private boolean enabled = false;
    }
}
