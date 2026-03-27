package com.nexusmall.thirdparty.controller;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.thirdparty.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * MinIO 文件上传控制器
 * 
 * 业界标准做法：
 * 1. RESTful API 设计
 * 2. 统一返回 Result 格式
 * 3. 支持多种上传场景（单文件、多文件、指定类型）
 * 4. 完善的异常处理
 * 5. Swagger 文档注释
 * 
 * @author NexusMall
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@RestController
@RequestMapping("/minio")
@Tag(name = "MinIO 文件管理", description = "提供文件上传、删除、查询等操作")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 单文件上传
     * 
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload")
    @Operation(summary = "单文件上传", description = "上传单个文件到 MinIO，返回访问 URL")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "上传的文件", required = true) 
            @RequestParam("file") MultipartFile file) {
        try {
            String url = minioService.uploadFile(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));
            
            log.info("文件上传成功：{} -> {}", file.getOriginalFilename(), url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败：{}", file.getOriginalFilename(), e);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getErrorCode(), "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 指定类型的文件上传
     * 
     * @param file 上传的文件
     * @param type 文件类型（product-商品、avatar-头像、order-订单等）
     * @return 文件访问 URL
     */
    @PostMapping("/upload/{type}")
    @Operation(summary = "指定类型的文件上传", description = "按业务类型分类存储文件")
    public Result<Map<String, String>> uploadFileByType(
            @Parameter(description = "上传的文件", required = true) 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型：product-商品、avatar-头像、order-订单等", required = true)
            @PathVariable("type") String type) {
        try {
            String url = minioService.uploadFile(file, type);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("type", type);
            result.put("filename", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));
            
            log.info("文件上传成功 [类型={}]: {} -> {}", type, file.getOriginalFilename(), url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败 [类型={}]: {}", type, file.getOriginalFilename(), e);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getErrorCode(), "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     * 
     * @param url 文件 URL
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "从 MinIO 中删除指定文件")
    public Result<Void> deleteFile(
            @Parameter(description = "文件 URL", required = true)
            @RequestParam("url") String url) {
        try {
            minioService.deleteFile(url);
            log.info("文件删除成功：{}", url);
            return Result.success(null);
        } catch (Exception e) {
            log.error("文件删除失败：{}", url, e);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getErrorCode(), "文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     * 
     * @param files 多个文件
     * @return 文件 URL 列表
     */
    @PostMapping("/batch-upload")
    @Operation(summary = "批量上传文件", description = "一次性上传多个文件")
    public Result<Map<String, Object>> batchUploadFiles(
            @Parameter(description = "多个文件", required = true)
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return Result.failure(CommonResultCode.SYSTEM_ERROR.getErrorCode(), "请选择要上传的文件");
            }
            
            java.util.List<String> urls = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                String url = minioService.uploadFile(file);
                urls.add(url);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("urls", urls);
            result.put("count", urls.size());
            
            log.info("批量上传成功：共 {} 个文件", urls.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量上传失败", e);
            return Result.failure(CommonResultCode.SYSTEM_ERROR.getErrorCode(), "批量上传失败：" + e.getMessage());
        }
    }
}
