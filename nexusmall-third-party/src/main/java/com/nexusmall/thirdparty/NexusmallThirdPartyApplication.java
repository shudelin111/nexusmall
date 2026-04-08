package com.nexusmall.thirdparty;

import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.thirdparty.service.MinioService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

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
@Import({GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {NexusmallThirdPartyApplication.class})
public class NexusmallThirdPartyApplication {

    private final MinioService minioService;

    public NexusmallThirdPartyApplication(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 应用启动时初始化 MinIO 存储桶
     */
    @PostConstruct
    public void init() {
        minioService.initBucket();
    }

    public static void main(String[] args) {
        SpringApplication.run(NexusmallThirdPartyApplication.class, args);
    }
}
