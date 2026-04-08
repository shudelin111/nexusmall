package com.nexusmall.payment.domain.constants;

/**
 * 支付渠道编码常量
 *
 * @author shudl
 * @since 2026-04-06
 */
public class PayChannelCode {

    /**
     * 支付宝
     */
    public static final String ALIPAY = "ALIPAY";

    /**
     * 微信支付
     */
    public static final String WECHAT_PAY = "WECHAT_PAY";

    /**
     * 银联支付
     */
    public static final String UNION_PAY = "UNION_PAY";

    private PayChannelCode() {
        // 防止实例化
    }
}
