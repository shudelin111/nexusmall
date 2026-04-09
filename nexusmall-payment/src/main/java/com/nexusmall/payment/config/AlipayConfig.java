package com.nexusmall.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配?
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String merchantPrivateKey;

    /**
     * 支付宝公?
     */
    private String alipayPublicKey;

    /**
     * 网关地址(沙箱: https://openapi.alipaydev.com/gateway.do)
     * 正式: https://openapi.alipay.com/gateway.do
     */
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 字符编码
     */
    private String charset = "UTF-8";

    /**
     * 返回数据格式
     */
    private String format = "json";

    /**
     * 签名类型
     */
    private String signType = "RSA2";

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步返回地址
     */
    private String returnUrl;
}
