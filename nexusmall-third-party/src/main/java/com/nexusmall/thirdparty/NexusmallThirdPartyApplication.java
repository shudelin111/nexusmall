package com.nexusmall.thirdparty;

import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 第三方服务启动类
 * <p>
 * 负责 OSS 文件存储、短信发送等第三方服务集成
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(RedisConfig.class)
@ComponentScan(basePackageClasses = {NexusmallThirdPartyApplication.class, RedisUtils.class})
public class NexusmallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallThirdPartyApplication.class, args);
    }
}
