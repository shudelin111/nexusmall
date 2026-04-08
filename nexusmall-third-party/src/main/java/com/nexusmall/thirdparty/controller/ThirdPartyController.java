package com.nexusmall.thirdparty.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.ThirdPartyException;
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
@RequestMapping("/")  // Gateway 已通过 /third-party/** 路由,StripPrefix 后直接访问
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
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
    @PostMapping(value = "/sms/send", headers = "X-API-Version=v1")
    public SmsSendResponse sendSms(@RequestBody @Valid SmsSendRequest request) {
        if (smsService == null) {
            throw new ThirdPartyException(CommonResultCode.SMS_SERVICE_ERROR.getErrorCode(), CommonResultCode.SMS_SERVICE_ERROR.getMessage());
        }
        return smsService.sendSms(request);
    }

    /**
     * 上传文件到OSS。
     */
    @PostMapping(value = "/oss/upload", headers = "X-API-Version=v1")
    public OssUploadResponse upload(@RequestPart("file") MultipartFile file,
                                    @RequestParam(value = "dir", required = false) String dir) {
        if (ossService == null) {
            throw new ThirdPartyException(CommonResultCode.OSS_CONFIG_ERROR.getErrorCode(), CommonResultCode.OSS_CONFIG_ERROR.getMessage());
        }
        return ossService.upload(file, dir);
    }
}
