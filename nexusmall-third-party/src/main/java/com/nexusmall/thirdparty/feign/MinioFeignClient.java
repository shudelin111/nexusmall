package com.nexusmall.thirdparty.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * MinIO 文件服务 Feign 客户端
 * 
 * 业界标准做法：
 * 1. 使用 Feign 实现微服务间调用
 * 2. 统一使用 Result 包装返回结果
 * 3. 支持降级处理（可选）
 * 
 * @author NexusMall
 * @since 0.0.1-SNAPSHOT
 */
@FeignClient(name = "nexusmall-third-party", contextId = "minioFeignClient")
public interface MinioFeignClient {

    /**
     * 上传文件到指定类型目录
     * 
     * @param file 文件
     * @param type 类型（product、avatar、order 等）
     * @return 文件访问 URL
     */
    @PostMapping("/minio/upload/{type}")
    Result<Map<String, String>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @PathVariable("type") String type);

    /**
     * 删除文件
     * 
     * @param url 文件 URL
     * @return 操作结果
     */
    @DeleteMapping("/minio/delete")
    Result<Void> deleteFile(@RequestParam("url") String url);
}
