package com.nexusmall.notification.application.service;

/**
 * 推送通知服务接口
 * <p>
 * 业界标准（Push Notification Service）：
 * - 支持 iOS APNs 和 Android FCM
 * - 支持国内厂商推送（华为、小米、OPPO、vivo）
 * - 支持自定义消息和通知消息
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface PushService {

    /**
     * 发送单设备推送
     *
     * @param deviceId 设备ID
     * @param title    标题
     * @param content  内容
     * @param extras   扩展参数
     * @return 是否成功
     */
    boolean sendToDevice(String deviceId, String title, String content, java.util.Map<String, String> extras);

    /**
     * 批量推送
     *
     * @param deviceIds 设备ID列表
     * @param title     标题
     * @param content   内容
     * @param extras    扩展参数
     * @return 成功数量
     */
    int batchSend(java.util.List<String> deviceIds, String title, String content, java.util.Map<String, String> extras);

    /**
     * 全员推送
     *
     * @param title   标题
     * @param content 内容
     * @param extras  扩展参数
     * @return 是否成功
     */
    boolean sendToAll(String title, String content, java.util.Map<String, String> extras);
}
