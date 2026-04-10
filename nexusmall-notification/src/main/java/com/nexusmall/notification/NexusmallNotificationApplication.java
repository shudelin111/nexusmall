package com.nexusmall.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * NexusMall 通知服务启动类
 * <p>
 * 职责：
 * - 站内消息管理（系统通知/订单状态/营销活动）
 * - 短信发送（验证码/订单提醒/营销短信）
 * - 邮件发送（注册验证/订单确认/找回密码）
 * - 推送通知（APP Push/微信模板消息）
 * - 消息模板管理
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NexusmallNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallNotificationApplication.class, args);
    }
}
