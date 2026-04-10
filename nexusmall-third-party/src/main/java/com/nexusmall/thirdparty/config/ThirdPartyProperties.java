package com.nexusmall.thirdparty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取 application.yaml 中 third-party.* 的配置。
 * 这里只做“配置映射”，不放业务逻辑。
 */
@ConfigurationProperties(prefix = "third-party")
public class ThirdPartyProperties {

    /** 短信配置：third-party.sms.* */
    private Sms sms = new Sms();
    /** 存储配置：third-party.storage.* */
    private Storage storage = new Storage();

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public static class Sms {
        /** 是否启用短信能力 */
        private Boolean enabled;
        /** 短信服务商，如 aliyun/tencent/huawei */
        private String provider;
        /** 阿里云短信参数 */
        private Aliyun aliyun = new Aliyun();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Aliyun getAliyun() {
            return aliyun;
        }

        public void setAliyun(Aliyun aliyun) {
            this.aliyun = aliyun;
        }
    }

    public static class Storage {
        /** 是否启用对象存储能力 */
        private Boolean enabled;
        /** 存储服务商，如 qiniu/aliyun-oss/tencent-cos */
        private String provider;
        /** 阿里云 OSS 参数 */
        private AliyunOss aliyunOss = new AliyunOss();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public AliyunOss getAliyunOss() {
            return aliyunOss;
        }

        public void setAliyunOss(AliyunOss aliyunOss) {
            this.aliyunOss = aliyunOss;
        }
    }

    public static class Aliyun {
        /** 阿里云区域，如 cn-hangzhou */
        private String regionId;
        /** 阿里云 AccessKeyId */
        private String accessKeyId;
        /** 阿里云 AccessKeySecret */
        private String accessKeySecret;
        /** 短信签名 */
        private String signName;
        /** 短信模板编码 */
        private String templateCode;

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = signName;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }
    }

    public static class AliyunOss {
        /** OSS 访问域名，如 oss-cn-hangzhou.aliyuncs.com */
        private String endpoint;
        /** 阿里云 AccessKeyId */
        private String accessKeyId;
        /** 阿里云 AccessKeySecret */
        private String accessKeySecret;
        /** OSS bucket 名称 */
        private String bucketName;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }
    }
}
