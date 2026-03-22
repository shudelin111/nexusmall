package com.nexusmall.thirdparty.controller;

import com.nexusmall.thirdparty.service.OssService;
import com.nexusmall.thirdparty.service.SmsService;
import com.nexusmall.thirdparty.vo.OssUploadResponse;
import com.nexusmall.thirdparty.vo.SmsSendRequest;
import com.nexusmall.thirdparty.vo.SmsSendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 第三方能力对外接口：短信、OSS。
 */
@Validated
@RestController
@RequestMapping("/third-party")
public class ThirdPartyController {

    /**
     * required=false：当短信未启用时，不会注入实现类。
     */
    @Autowired(required = false)
    private SmsService smsService;

    /**
     * required=false：当OSS未启用时，不会注入实现类。
     */
    @Autowired(required = false)
    private OssService ossService;

    /**
     * 发送短信。
     */
    @PostMapping("/sms/send")
    public SmsSendResponse sendSms(@RequestBody @Valid SmsSendRequest request) {
        if (smsService == null) {
            throw new IllegalStateException("短信服务未启用或未正确配置");
        }
        return smsService.sendSms(request);
    }

    /**
     * 上传文件到OSS。
     */
    @PostMapping("/oss/upload")
    public OssUploadResponse upload(@RequestPart("file") MultipartFile file,
                                    @RequestParam(value = "dir", required = false) String dir) {
        if (ossService == null) {
            throw new IllegalStateException("OSS服务未启用或未正确配置");
        }
        return ossService.upload(file, dir);
    }
}
