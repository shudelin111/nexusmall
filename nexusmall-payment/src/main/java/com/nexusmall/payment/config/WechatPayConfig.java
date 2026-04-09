package com.nexusmall.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatPayConfig {

    /**
     * 商户端
     */
    private String mchId;

    /**
     * 商户API私钥路径
     */
    private String privateKeyPath;

    /**
     * 商户证书序列表
     */
    private String merchantSerialNumber;

    /**
     * 商户APIV3密钥
     */
    private String apiV3Key;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * API基础URL(沙箱: https://api.mch.weixin.qq.com/sandboxnew)
     * 正式: https://api.mch.weixin.qq.com
     */
    private String baseUrl = "https://api.mch.weixin.qq.com";
}
