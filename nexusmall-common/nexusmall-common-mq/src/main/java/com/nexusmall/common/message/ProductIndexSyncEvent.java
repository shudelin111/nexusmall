package com.nexusmall.common.message;

import java.io.Serializable;
public class ProductIndexSyncEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private ProductIndexSyncEventType eventType;
    private Long occurredAt;

    public ProductIndexSyncEvent() {
    }

    public ProductIndexSyncEvent(Long productId, ProductIndexSyncEventType eventType, Long occurredAt) {
        this.productId = productId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public ProductIndexSyncEventType getEventType() {
        return eventType;
    }

    public void setEventType(ProductIndexSyncEventType eventType) {
        this.eventType = eventType;
    }

    public Long getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Long occurredAt) {
        this.occurredAt = occurredAt;
    }
}
