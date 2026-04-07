package com.nexusmall.member.application.service;

/**
 * 会员等级升级通知服务
 * <p>
 * 业界标准：
 * - 支持多渠道通知(短信/邮件/Push)
 * - 异步发送,不影响主流程
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface MemberLevelUpgradeNotificationService {

    /**
     * 发送等级升级通知
     *
     * @param memberId   会员 ID
     * @param oldLevelName 旧等级名称
     * @param newLevelName 新等级名称
     */
    void sendUpgradeNotification(Long memberId, String oldLevelName, String newLevelName);

    /**
     * 发送短信通知
     *
     * @param phone      手机号
     * @param levelName  新等级名称
     */
    void sendSmsNotification(String phone, String levelName);

    /**
     * 发送邮件通知
     *
     * @param email     邮箱
     * @param levelName 新等级名称
     */
    void sendEmailNotification(String email, String levelName);
}
