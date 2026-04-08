package com.nexusmall.search.infrastructure.messaging.consumer;

import com.nexusmall.common.message.ProductIndexSyncEvent;
import com.nexusmall.common.message.ProductIndexSyncEventType;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.application.service.IndexApplicationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "${nexusmall.search.product-sync-topic}", consumerGroup = "nexusmall-search-index-sync")
public class ProductIndexSyncConsumer implements RocketMQListener<ProductIndexSyncEvent> {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexSyncConsumer.class);

    private final IndexApplicationService indexApplicationService;

    public ProductIndexSyncConsumer(IndexApplicationService indexApplicationService) {
        this.indexApplicationService = indexApplicationService;
    }

    @Override
    public void onMessage(ProductIndexSyncEvent event) {
        if (event == null || event.getProductId() == null || event.getEventType() == null) {
            log.warn("Ignore invalid product index sync event: {}", event);
            return;
        }
        if (event.getEventType() == ProductIndexSyncEventType.UPSERT) {
            indexApplicationService.syncProductIndex(event.getProductId());
            return;
        }
        RemoveProductIndexCommand command = new RemoveProductIndexCommand();
        command.setProductId(event.getProductId());
        indexApplicationService.removeProductIndex(command);
    }
}
