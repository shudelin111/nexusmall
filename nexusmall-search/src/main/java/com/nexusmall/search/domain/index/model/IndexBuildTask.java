package com.nexusmall.search.domain.index.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IndexBuildTask {

    private String indexAlias;
    private boolean fullRebuild;
    private LocalDateTime scheduledAt;
    private List<Long> productIds = new ArrayList<Long>();

    public String getIndexAlias() {
        return indexAlias;
    }

    public void setIndexAlias(String indexAlias) {
        this.indexAlias = indexAlias;
    }

    public boolean isFullRebuild() {
        return fullRebuild;
    }

    public void setFullRebuild(boolean fullRebuild) {
        this.fullRebuild = fullRebuild;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
