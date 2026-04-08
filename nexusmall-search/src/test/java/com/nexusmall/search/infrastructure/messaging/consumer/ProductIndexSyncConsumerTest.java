package com.nexusmall.search.infrastructure.messaging.consumer;

import com.nexusmall.common.message.ProductIndexSyncEvent;
import com.nexusmall.common.message.ProductIndexSyncEventType;
import com.nexusmall.search.application.service.IndexApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProductIndexSyncConsumerTest {

    @Test
    void shouldSyncProductIndexOnUpsertEvent() {
        IndexApplicationService indexApplicationService = Mockito.mock(IndexApplicationService.class);
        ProductIndexSyncConsumer consumer = new ProductIndexSyncConsumer(indexApplicationService);

        ProductIndexSyncEvent event = new ProductIndexSyncEvent(1001L, ProductIndexSyncEventType.UPSERT, System.currentTimeMillis());
        consumer.onMessage(event);

        Mockito.verify(indexApplicationService).syncProductIndex(1001L);
        Mockito.verify(indexApplicationService, Mockito.never()).removeProductIndex(Mockito.any());
    }

    @Test
    void shouldRemoveProductIndexOnRemoveEvent() {
        IndexApplicationService indexApplicationService = Mockito.mock(IndexApplicationService.class);
        ProductIndexSyncConsumer consumer = new ProductIndexSyncConsumer(indexApplicationService);

        ProductIndexSyncEvent event = new ProductIndexSyncEvent(1002L, ProductIndexSyncEventType.REMOVE, System.currentTimeMillis());
        consumer.onMessage(event);

        Mockito.verify(indexApplicationService).removeProductIndex(Mockito.argThat(command ->
                command != null && Long.valueOf(1002L).equals(command.getProductId())));
    }

    @Test
    void shouldIgnoreInvalidEvent() {
        IndexApplicationService indexApplicationService = Mockito.mock(IndexApplicationService.class);
        ProductIndexSyncConsumer consumer = new ProductIndexSyncConsumer(indexApplicationService);

        consumer.onMessage(new ProductIndexSyncEvent());

        Mockito.verifyNoInteractions(indexApplicationService);
    }
}
