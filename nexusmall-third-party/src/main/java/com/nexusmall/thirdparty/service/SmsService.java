package com.nexusmall.thirdparty.service;

import com.nexusmall.thirdparty.vo.SmsSendRequest;
import com.nexusmall.thirdparty.vo.SmsSendResponse;

/**
 * 短信服务接口。
 */
public interface SmsService {

    /**
     * 发送短信。
     *
     * @param request 手机号和模板参数
     * @return 发送结果
     */
    SmsSendResponse sendSms(SmsSendRequest request);
}
