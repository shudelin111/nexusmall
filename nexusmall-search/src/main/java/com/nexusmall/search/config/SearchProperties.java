package com.nexusmall.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexusmall.search")
public class SearchProperties {

    private String indexAlias = "nexusmall_product";
    private int defaultPageNo = 1;
    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private int indexShards = 1;
    private int indexReplicas = 1;
    private String productSyncTopic = "PRODUCT_INDEX_SYNC_TOPIC";

    public String getIndexAlias() {
        return indexAlias;
    }

    public void setIndexAlias(String indexAlias) {
        this.indexAlias = indexAlias;
    }

    public int getDefaultPageNo() {
        return defaultPageNo;
    }

    public void setDefaultPageNo(int defaultPageNo) {
        this.defaultPageNo = defaultPageNo;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public int getIndexShards() {
        return indexShards;
    }

    public void setIndexShards(int indexShards) {
        this.indexShards = indexShards;
    }

    public int getIndexReplicas() {
        return indexReplicas;
    }

    public void setIndexReplicas(int indexReplicas) {
        this.indexReplicas = indexReplicas;
    }

    public String getProductSyncTopic() {
        return productSyncTopic;
    }

    public void setProductSyncTopic(String productSyncTopic) {
        this.productSyncTopic = productSyncTopic;
    }
}
