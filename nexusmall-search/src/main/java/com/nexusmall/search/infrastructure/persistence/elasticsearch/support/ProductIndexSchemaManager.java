package com.nexusmall.search.infrastructure.persistence.elasticsearch.support;

import com.nexusmall.search.config.SearchProperties;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductIndexSchemaManager {

    private final RestHighLevelClient client;
    private final SearchProperties searchProperties;

    public ProductIndexSchemaManager(RestHighLevelClient client, SearchProperties searchProperties) {
        this.client = client;
        this.searchProperties = searchProperties;
    }

    public String getIndexName() {
        return searchProperties.getIndexAlias();
    }

    public void ensureIndex() {
        GetIndexRequest request = new GetIndexRequest(getIndexName());
        try {
            if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
                client.indices().create(buildCreateRequest(), RequestOptions.DEFAULT);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to ensure search index", ex);
        }
    }

    public void recreateIndex() {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(getIndexName());
            if (client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                AcknowledgedResponse response = client.indices()
                        .delete(new DeleteIndexRequest(getIndexName()), RequestOptions.DEFAULT);
                if (!response.isAcknowledged()) {
                    throw new IllegalStateException("Failed to delete existing search index");
                }
            }
            client.indices().create(buildCreateRequest(), RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to recreate search index", ex);
        }
    }

    private CreateIndexRequest buildCreateRequest() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(getIndexName());
        request.settings(Settings.builder()
                .put("index.number_of_shards", searchProperties.getIndexShards())
                .put("index.number_of_replicas", searchProperties.getIndexReplicas()));
        request.mapping(buildMapping());
        return request;
    }

    private XContentBuilder buildMapping() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            keyword(builder, "productId", "long");
            textWithKeyword(builder, "name");
            textWithKeyword(builder, "subTitle");
            keyword(builder, "categoryId", "long");
            textWithKeyword(builder, "categoryName");
            keyword(builder, "brandId", "long");
            textWithKeyword(builder, "brandName");
            keyword(builder, "price", "double");
            keyword(builder, "mainImage", "keyword");
            keyword(builder, "stock", "integer");
            keyword(builder, "status", "integer");
            builder.startObject("searchableAttributes");
            builder.field("type", "keyword");
            builder.endObject();
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    private void textWithKeyword(XContentBuilder builder, String field) throws IOException {
        builder.startObject(field);
        builder.field("type", "text");
        builder.startObject("fields");
        builder.startObject("keyword");
        builder.field("type", "keyword");
        builder.field("ignore_above", 256);
        builder.endObject();
        builder.endObject();
        builder.endObject();
    }

    private void keyword(XContentBuilder builder, String field, String type) throws IOException {
        builder.startObject(field);
        builder.field("type", type);
        builder.endObject();
    }
}
