package com.nexusmall.thirdparty.vo;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 短信发送请求。
 */
public class SmsSendRequest {

    /**
     * 手机号，支持单个号码。
     */
    @NotBlank(message = "phoneNumber不能为空")
    private String phoneNumber;

    /**
     * 模板变量，最终会转成JSON传给阿里云。
     * 例如：{"code":"123456"}
     */
    private Map<String, Object> templateParam;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, Object> getTemplateParam() {
        return templateParam;
    }

    public void setTemplateParam(Map<String, Object> templateParam) {
        this.templateParam = templateParam;
    }
}
