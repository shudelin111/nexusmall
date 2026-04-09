package com.nexusmall.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 商品索引同步事件
 * <p>
 * 用于通知 Search 模块更新 Elasticsearch 索引
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProductIndexSyncEvent extends BaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 商品 ID
     */
    private Long productId;

    /**
     * 事件类型（CREATE/UPDATE/DELETE）
     */
    private ProductIndexSyncEventType eventType;

    /**
     * 事件发生时间
     */
    private Long occurredAt;

    public ProductIndexSyncEvent(Long productId, ProductIndexSyncEventType eventType, Long occurredAt) {
        super("product-index-" + productId, "product-service");
        this.productId = productId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }
}
