package com.nexusmall.thirdparty.service.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.ThirdPartyException;
import com.nexusmall.thirdparty.config.ThirdPartyProperties;
import com.nexusmall.thirdparty.service.SmsService;
import com.nexusmall.thirdparty.vo.SmsSendRequest;
import com.nexusmall.thirdparty.vo.SmsSendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信实现。
 * 只有 IAcsClient 已经被装配时，本类才会生效。
 */
@Service
@ConditionalOnBean(IAcsClient.class)
public class AliyunSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsServiceImpl.class);

    @Autowired
    private IAcsClient acsClient;

    @Autowired
    private ThirdPartyProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SmsSendResponse sendSms(SmsSendRequest request) {
        ThirdPartyProperties.Aliyun aliyun = properties.getSms().getAliyun();

        // 组装阿里云短信请求
        SendSmsRequest smsRequest = new SendSmsRequest();
        smsRequest.setPhoneNumbers(request.getPhoneNumber());
        smsRequest.setSignName(aliyun.getSignName());
        smsRequest.setTemplateCode(aliyun.getTemplateCode());

        // 模板变量转成JSON字符串
        if (request.getTemplateParam() != null && !request.getTemplateParam().isEmpty()) {
            try {
                smsRequest.setTemplateParam(objectMapper.writeValueAsString(request.getTemplateParam()));
            } catch (JsonProcessingException e) {
                log.error("短信模板参数序列化失败，phone: {}, 参数：{}, 错误：{}", 
                         request.getPhoneNumber(), request.getTemplateParam(), e.getMessage(), e);
                throw new ThirdPartyException(ResultCode.SMS_TEMPLATE_PARSE_FAILED, e);
            }
        }

        try {
            SendSmsResponse response = acsClient.getAcsResponse(smsRequest);

            // 转换为项目内统一响应对象
            SmsSendResponse result = new SmsSendResponse();
            result.setRequestId(response.getRequestId());
            result.setBizId(response.getBizId());
            result.setCode(response.getCode());
            result.setMessage(response.getMessage());
            result.setSuccess("OK".equalsIgnoreCase(response.getCode()));
            return result;
        } catch (ClientException e) {
            log.error("调用阿里云短信接口失败，phone: {}, 错误码：{}, 错误：{}", 
                     request.getPhoneNumber(), e.getErrCode(), e.getErrMsg(), e);
            throw new ThirdPartyException(ResultCode.SYSTEM_ERROR, e);
        }
    }
}
