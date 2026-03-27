package com.nexusmall.thirdparty.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置类
 * 
 * 业界标准做法：
 * 1. 使用 Spring Boot ConfigurationProperties 绑定配置
 * 2. 创建 MinioClient Bean，全局复用
 * 3. 支持多环境配置（开发/测试/生产）
 * 
 * @author NexusMall
 * @since 0.0.1-SNAPSHOT
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO 服务器地址
     * 格式：http://host:port 或 https://host:port
     */
    private String endpoint;

    /**
     * 访问密钥（Access Key）
     * 类似用户名，用于身份认证
     */
    private String accessKey;

    /**
     * 秘密密钥（Secret Key）
     * 类似密码，用于身份认证
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     * 用于存储所有上传的文件
     */
    private String bucketName;

    /**
     * 是否自动创建存储桶
     * 开发环境：true（方便调试）
     * 生产环境：false（需要手动创建，提高安全性）
     */
    private Boolean autoCreateBucket;

    /**
     * 文件上传最大大小（字节）
     * 默认：100MB
     */
    private Long maxFileSize;

    /**
     * 允许上传的文件后缀名列表
     * 用于安全校验，防止上传危险文件
     */
    private String[] allowedExtensions;

    /**
     * 创建 MinioClient Bean
     * 
     * 业界最佳实践：
     * 1. 使用单例模式，全局复用同一个客户端
     * 2. 设置合理的超时时间
     * 3. 启用 SSL（生产环境）
     * 
     * @return MinioClient 实例
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                // 生产环境建议开启 SSL
                // .sslEnabled(true)
                .build();
    }
}
