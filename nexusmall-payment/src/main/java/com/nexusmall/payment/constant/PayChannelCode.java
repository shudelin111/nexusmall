package com.nexusmall.payment.constant;

/**
 * 支付渠道编码常量
 * <p>
 * 业界标准：使用大写英文标识，便于扩展和维护
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public final class PayChannelCode {

    /**
     * 支付宝
     */
    public static final String ALIPAY = "ALIPAY";

    /**
     * 微信支付
     */
    public static final String WECHAT = "WECHAT";

    /**
     * 银联支付
     */
    public static final String UNIONPAY = "UNIONPAY";

    /**
     * 私有构造函数，防止实例化
     */
    private PayChannelCode() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
