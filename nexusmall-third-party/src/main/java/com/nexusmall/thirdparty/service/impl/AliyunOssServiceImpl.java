package com.nexusmall.thirdparty.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.ThirdPartyException;
import com.nexusmall.thirdparty.config.ThirdPartyProperties;
import com.nexusmall.thirdparty.service.OssService;
import com.nexusmall.thirdparty.vo.OssUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 服务实现类
 * <p>
 * 采用 Spring 条件装配模式，仅在配置了 OSS 客户端时生效
 * 遵循资源自动管理、异常分类处理、日志完整记录的最佳实践
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Service
@ConditionalOnBean(OSS.class)
public class AliyunOssServiceImpl implements OssService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssServiceImpl.class);

    /**
     * OSS 客户端（由 Spring 容器管理生命周期）
     */
    private final OSS ossClient;

    /**
     * 第三方服务配置属性
     */
    private final ThirdPartyProperties properties;

    /**
     * 构造器注入（推荐做法，便于测试）
     *
     * @param ossClient OSS 客户端
     * @param properties 配置属性
     */
    public AliyunOssServiceImpl(OSS ossClient, ThirdPartyProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    @Override
    public OssUploadResponse upload(MultipartFile file, String dir) {
        // 1. 参数校验
        validateFile(file);
    
        // 2. 获取配置
        ThirdPartyProperties.AliyunOss ossConfig = properties.getStorage().getAliyunOss();
        String bucket = validateBucket(ossConfig.getBucketName());
    
        // 3. 生成对象 Key
        String objectKey = buildObjectKey(file.getOriginalFilename(), dir);
    
        // 4. 准备元数据
        ObjectMetadata metadata = createMetadata(file);
    
        try (InputStream inputStream = file.getInputStream()) {
            // 5. 上传文件
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, inputStream, metadata);
            PutObjectResult result = ossClient.putObject(request);
    
            // 6. 构建响应
            return buildUploadResponse(bucket, objectKey, result);
        } catch (IOException e) {
            log.error("OSS 文件上传失败，文件名：{}, 目录：{}, 错误：{}", 
                     file.getOriginalFilename(), dir, e.getMessage(), e);
            throw new ThirdPartyException(ResultCode.FILE_UPLOAD_FAILED, e);
        }
    }
    
    /**
     * 验证上传文件
     *
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ThirdPartyException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 验证存储桶名称
     *
     * @param bucketName 存储桶名称
     * @return 有效的存储桶名称
     */
    private String validateBucket(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new ThirdPartyException(ResultCode.OSS_CONFIG_ERROR);
        }
        return bucketName.trim();
    }
    
    /**
     * 创建对象元数据
     *
     * @param file 上传的文件
     * @return 对象元数据
     */
    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        return metadata;
    }
    
    /**
     * 构建上传响应
     *
     * @param bucket 存储桶
     * @param objectKey 对象键
     * @param result 上传结果
     * @return 上传响应
     */
    private OssUploadResponse buildUploadResponse(String bucket, String objectKey, PutObjectResult result) {
        OssUploadResponse response = new OssUploadResponse();
        response.setBucket(bucket);
        response.setObjectKey(objectKey);
        response.setEtag(result.getETag());
        response.setUrl(buildFileUrl(bucket, objectKey));
        return response;
    }

    /**
     * 生成最终对象 Key
     * <p>
     * 规则：目录前缀 + UUID 随机文件名 + 原文件后缀
     * </p>
     *
     * @param originalFilename 原始文件名
     * @param dir 目录前缀
     * @return 对象 Key
     */
    private String buildObjectKey(String originalFilename, String dir) {
        // 提取文件扩展名
        String suffix = getFileSuffix(originalFilename);
            
        // 生成随机文件名
        String randomName = generateRandomFileName(suffix);
    
        // 处理目录前缀
        if (dir == null || dir.trim().isEmpty()) {
            return randomName;
        }
    
        // 规范化目录：去除首尾斜杠后，统一添加尾部斜杠
        String normalizedDir = normalizeDirectoryPath(dir.trim());
        return normalizedDir + randomName;
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名（包含点号），如果没有扩展名则返回空字符串
     */
    private String getFileSuffix(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot) : "";
    }
    
    /**
     * 生成随机文件名
     *
     * @param suffix 文件扩展名
     * @return UUID 随机文件名
     */
    private String generateRandomFileName(String suffix) {
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }
    
    /**
     * 规范化目录路径
     * <p>
     * 确保目录以 / 结尾且不包含开头的 /
     * </p>
     *
     * @param dir 原始目录
     * @return 规范化后的目录
     */
    private String normalizeDirectoryPath(String dir) {
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        return dir;
    }

    /**
     * 组装文件访问 URL
     * <p>
     * 格式：{endpoint}/{bucket}/{objectKey}
     * </p>
     *
     * @param bucket 存储桶
     * @param objectKey 对象键
     * @return 完整的访问 URL
     */
    private String buildFileUrl(String bucket, String objectKey) {
        String endpoint = properties.getStorage().getAliyunOss().getEndpoint();
            
        // 如果 endpoint 未配置，直接返回 objectKey
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return objectKey;
        }
    
        // 确保 endpoint 包含协议头
        String host = normalizeEndpoint(endpoint.trim());
        return String.format("%s/%s/%s", host, bucket, objectKey);
    }
    
    /**
     * 规范化 Endpoint，确保包含协议头
     *
     * @param endpoint 原始 Endpoint
     * @return 带协议头的 Endpoint
     */
    private String normalizeEndpoint(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "https://" + endpoint;
    }
}
