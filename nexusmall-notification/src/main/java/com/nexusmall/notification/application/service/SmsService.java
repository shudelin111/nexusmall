package com.nexusmall.notification.application.service;

/**
 * 短信发送服务接口
 * <p>
 * 业界标准（Multi-Channel Notification）：
 * - 支持多渠道短信服务商（阿里云、腾讯云等）
 * - 提供统一的短信发送接口
 * - 支持模板短信和验证码短信
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface SmsService {

    /**
     * 发送模板短信
     *
     * @param phone      手机号
     * @param templateId 模板ID
     * @param params     模板参数
     * @return 是否成功
     */
    boolean sendTemplateSms(String phone, String templateId, Object... params);

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否成功
     */
    boolean sendVerificationCode(String phone, String code);

    /**
     * 批量发送短信
     *
     * @param phones     手机号列表
     * @param templateId 模板ID
     * @param params     模板参数
     * @return 成功数量
     */
    int batchSendSms(java.util.List<String> phones, String templateId, Object... params);
}
