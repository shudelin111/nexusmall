package com.nexusmall.notification;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.RedissonConfig;
import com.nexusmall.common.config.SeataDataSourceConfig;
import com.nexusmall.common.config.SeataFeignConfig;
import com.nexusmall.common.filter.SeataXidFilter;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

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
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, SeataFeignConfig.class, GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallNotificationApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallNotificationApplication.class, args);
    }
}
