package com.nexusmall.member;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
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
 * NexusMall 会员服务启动类
 * <p>
 * 职责：
 * - 会员档案管理（昵称/头像/生日/性别）
 * - 收货地址管理
 * - 会员等级/积分/成长值管理
 * - 监听用户注册事件，自动创建会员档案
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({SeataFeignConfig.class, GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallMemberApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallMemberApplication.class, args);
    }
}
