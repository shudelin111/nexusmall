package com.nexusmall.thirdparty.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
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
import java.util.UUID;

/**
 * 阿里云OSS实现。
 * 只有 OSS 客户端被装配时，本类才会生效。
 */
@Service
@ConditionalOnBean(OSS.class)
public class AliyunOssServiceImpl implements OssService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssServiceImpl.class);

    @Autowired
    private OSS ossClient;

    @Autowired
    private ThirdPartyProperties properties;

    @Override
    public OssUploadResponse upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String bucket = properties.getStorage().getAliyunOss().getBucketName();
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalStateException("未配置OSS bucket-name");
        }

        // 生成对象key（目录 + 随机文件名）
        String objectKey = buildObjectKey(file.getOriginalFilename(), dir);

        // 填充元信息，保留文件大小和类型
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file.getInputStream(), metadata);
            PutObjectResult result = ossClient.putObject(request);

            // 返回上传结果给调用方
            OssUploadResponse response = new OssUploadResponse();
            response.setBucket(bucket);
            response.setObjectKey(objectKey);
            response.setEtag(result.getETag());
            response.setUrl(buildFileUrl(bucket, objectKey));
            return response;
        } catch (IOException e) {
            log.error("OSS 文件上传失败，文件名：{}, 错误：{}", file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("读取上传文件失败", e);
        }
    }

    /**
     * 生成最终对象Key。
     */
    private String buildObjectKey(String originalFilename, String dir) {
        String filename = originalFilename == null ? "" : originalFilename;
        int dot = filename.lastIndexOf('.');
        String suffix = dot >= 0 ? filename.substring(dot) : "";
        String randomName = UUID.randomUUID().toString().replace("-", "") + suffix;

        if (dir == null || dir.trim().isEmpty()) {
            return randomName;
        }

        String normalizedDir = dir.trim();
        if (normalizedDir.startsWith("/")) {
            normalizedDir = normalizedDir.substring(1);
        }
        if (!normalizedDir.endsWith("/")) {
            normalizedDir = normalizedDir + "/";
        }
        return normalizedDir + randomName;
    }

    /**
     * 组装访问URL。
     */
    private String buildFileUrl(String bucket, String objectKey) {
        String endpoint = properties.getStorage().getAliyunOss().getEndpoint();
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return objectKey;
        }

        String host = endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : "https://" + endpoint;
        return host + "/" + bucket + "/" + objectKey;
    }
}
