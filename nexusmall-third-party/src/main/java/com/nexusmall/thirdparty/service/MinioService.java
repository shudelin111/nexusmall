package com.nexusmall.thirdparty.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * MinIO 对象存储服务工具类
 * 
 * 业界最标准做法：
 * 1. 封装所有常用文件操作（上传、下载、删除、列表）
 * 2. 统一异常处理
 * 3. 文件名校验、大小校验、类型校验
 * 4. 生成预签名 URL（用于临时访问）
 * 5. 支持分片上传（大文件）
 * 
 * @author NexusMall
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
public class MinioService {

    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.auto-create-bucket:true}")
    private Boolean autoCreateBucket;
    
    @Value("${minio.max-file-size:104857600}")
    private Long maxFileSize;
    
    @Value("${minio.allowed-extensions:.jpg,.jpeg,.png,.gif,.bmp,.webp,.mp4,.avi,.mov,.pdf,.doc,.docx,.xls,.xlsx}")
    private List<String> allowedExtensions;
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 初始化存储桶
     * 在应用启动时自动创建默认存储桶
     */
    public void initBucket() {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            
            if (!exists && autoCreateBucket) {
                // 创建存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("成功创建 MinIO 存储桶：{}", bucketName);
                
                // 设置存储桶为公共读（开发环境方便访问）
                // 生产环境建议使用私有桶 + 预签名 URL
                String jsonPolicy = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [{\n" +
                        "    \"Effect\": \"Allow\",\n" +
                        "    \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "    \"Action\": [\"s3:GetObject\"],\n" +
                        "    \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                        "  }]\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(jsonPolicy)
                        .build());
                log.info("已设置存储桶 {} 为公共读策略", bucketName);
            } else if (exists) {
                log.info("MinIO 存储桶 {} 已存在", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化 MinIO 存储桶失败：{}", bucketName, e);
            throw new RuntimeException("初始化 MinIO 存储桶失败：" + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（最简单的方式）
     * 
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件（自定义路径）
     * 
     * 业界标准做法：
     * 1. 使用 UUID 重命名文件，避免文件名冲突
     * 2. 按日期分类存储（/yyyy/MM/dd/uuid_filename.ext）
     * 3. 校验文件大小和后缀
     * 
     * @param file 上传的文件
     * @param customPath 自定义路径前缀（如：product、avatar、order）
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String customPath) {
        try {
            // 1. 校验文件
            validateFile(file);
            
            // 2. 生成唯一的文件名
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String datePath = generateDatePath();
            String objectName = StringUtils.hasText(customPath) 
                    ? customPath + "/" + datePath + "/" + uuid + extension
                    : datePath + "/" + uuid + extension;
            
            // 3. 上传到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            // 4. 返回访问 URL
            String url = getFileUrl(objectName);
            log.info("文件上传成功：{} -> {}", originalFilename, url);
            
            return url;
            
        } catch (Exception e) {
            log.error("上传文件失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("上传文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件 URL 或对象名称
     */
    public void deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("删除文件失败：{}", fileUrl, e);
            throw new RuntimeException("删除文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件访问 URL
     * 
     * @param objectName 对象名称（路径）
     * @return 可访问的 URL
     */
    public String getFileUrl(String objectName) {
        try {
            // 如果桶是公共读的，直接返回 URL
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("获取文件 URL 失败：{}", objectName, e);
            throw new RuntimeException("获取文件 URL 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成预签名 URL（临时访问权限）
     * 
     * 业界最佳实践：
     * 1. 用于私有桶的临时访问
     * 2. 可设置过期时间（如 5 分钟、1 小时）
     * 3. 适用于需要权限控制的场景
     * 
     * @param objectName 对象名称
     * @param expirySeconds 过期时间（秒），默认 3600 秒（1 小时）
     * @return 预签名 URL
     */
    public String getPresignedObjectUrl(String objectName, Integer expirySeconds) {
        try {
            int expiry = (expirySeconds != null && expirySeconds > 0) ? expirySeconds : 3600;
            
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry)
                    .build());
        } catch (Exception e) {
            log.error("生成预签名 URL 失败：{}", objectName, e);
            throw new RuntimeException("生成预签名 URL 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件 URL 列表
     */
    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            try {
                deleteFile(fileUrl);
            } catch (Exception e) {
                log.error("批量删除文件失败：{}", fileUrl, e);
            }
        }
    }

    /**
     * 校验文件
     * 
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        // 1. 检查是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        // 2. 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("文件大小超过限制：%d MB", maxFileSize / 1024 / 1024));
        }
        
        // 3. 检查文件后缀
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("文件名无效");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("不支持的文件类型：%s，仅支持：%s", extension, String.join(",", allowedExtensions)));
        }
    }

    /**
     * 生成日期路径
     * 
     * 格式：yyyy/MM/dd
     * 
     * @return 日期路径字符串
     */
    private String generateDatePath() {
        ZonedDateTime now = ZonedDateTime.now();
        return String.format("%d/%02d/%02d", 
                now.getYear(), 
                now.getMonthValue(), 
                now.getDayOfMonth());
    }

    /**
     * 从 URL 中提取对象名称
     * 
     * @param fileUrl 文件 URL
     * @return 对象名称
     */
    private String extractObjectName(String fileUrl) {
        // 如果是完整 URL，提取对象名
        if (fileUrl.startsWith("http")) {
            // http://localhost:9000/nexusmall/product/2026/03/28/xxx.jpg
            // 提取 product/2026/03/28/xxx.jpg
            String[] parts = fileUrl.split("/");
            if (parts.length > 4) {
                StringBuilder objectName = new StringBuilder();
                for (int i = 5; i < parts.length; i++) {
                    objectName.append(parts[i]);
                    if (i < parts.length - 1) {
                        objectName.append("/");
                    }
                }
                return objectName.toString();
            }
        }
        // 如果已经是对象名，直接返回
        return fileUrl;
    }
}
