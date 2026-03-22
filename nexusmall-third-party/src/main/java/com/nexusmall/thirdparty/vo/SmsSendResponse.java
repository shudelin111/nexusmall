package com.nexusmall.thirdparty.vo;

/**
 * 短信发送响应。
 */
public class SmsSendResponse {

    /** 是否发送成功（阿里云 code=OK） */
    private boolean success;
    /** 平台请求ID */
    private String requestId;
    /** 短信回执ID */
    private String bizId;
    /** 平台返回码 */
    private String code;
    /** 平台返回信息 */
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
