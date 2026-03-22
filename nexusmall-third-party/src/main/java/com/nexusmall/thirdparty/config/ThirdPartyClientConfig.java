package com.nexusmall.thirdparty.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方客户端装配：
 * 1) 配置满足条件时才创建对应客户端 Bean
 * 2) 配置不满足时不创建，避免启动时报错
 */
@Configuration
@EnableConfigurationProperties(ThirdPartyProperties.class)
public class ThirdPartyClientConfig {

    /**
     * 创建阿里云短信客户端。
     * 触发条件：third-party.sms.enabled=true 且 provider=aliyun
     */
    @Bean
    @ConditionalOnExpression(
            "'${third-party.sms.enabled:false}' == 'true' and '${third-party.sms.provider:}' == 'aliyun'"
    )
    public IAcsClient aliyunSmsClient(ThirdPartyProperties properties) {
        ThirdPartyProperties.Aliyun aliyun = properties.getSms().getAliyun();
        DefaultProfile profile = DefaultProfile.getProfile(
                aliyun.getRegionId(),
                aliyun.getAccessKeyId(),
                aliyun.getAccessKeySecret()
        );
        return new DefaultAcsClient(profile);
    }

    /**
     * 创建阿里云 OSS 客户端。
     * 触发条件：third-party.storage.enabled=true 且 provider=aliyun-oss
     * destroyMethod=shutdown 用于应用关闭时释放连接资源。
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnExpression(
            "'${third-party.storage.enabled:false}' == 'true' and '${third-party.storage.provider:}' == 'aliyun-oss'"
    )
    public OSS aliyunOssClient(ThirdPartyProperties properties) {
        ThirdPartyProperties.AliyunOss aliyunOss = properties.getStorage().getAliyunOss();
        return new OSSClientBuilder().build(
                aliyunOss.getEndpoint(),
                aliyunOss.getAccessKeyId(),
                aliyunOss.getAccessKeySecret()
        );
    }
}
