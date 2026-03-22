package com.nexusmall.thirdparty.vo;

/**
 * OSS上传响应。
 */
public class OssUploadResponse {

    /** 可访问URL（按 endpoint + bucket + objectKey 组装） */
    private String url;
    /** 存储桶 */
    private String bucket;
    /** 对象key */
    private String objectKey;
    /** OSS返回的ETag */
    private String etag;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
