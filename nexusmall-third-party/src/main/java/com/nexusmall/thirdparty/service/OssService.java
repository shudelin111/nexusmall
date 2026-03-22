package com.nexusmall.thirdparty.service;

import com.nexusmall.thirdparty.vo.OssUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务接口。
 */
public interface OssService {

    /**
     * 上传文件到对象存储。
     *
     * @param file 上传文件
     * @param dir  目录前缀，可为空
     * @return 上传结果
     */
    OssUploadResponse upload(MultipartFile file, String dir);
}
