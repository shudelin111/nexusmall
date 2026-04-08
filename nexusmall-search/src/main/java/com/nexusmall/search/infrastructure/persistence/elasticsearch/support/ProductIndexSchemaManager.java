package com.nexusmall.search.infrastructure.persistence.elasticsearch.support;

import com.nexusmall.search.config.SearchProperties;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductIndexSchemaManager {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexSchemaManager.class);

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

    /**
     * 零停机重建索引（生产环境标准）
     * <p>
     * 流程：
     * 1. 创建新索引（带时间戳后缀）
     * 2. 从旧索引迁移数据到新索引
     * 3. 原子切换别名（用户无感知）
     * 4. 删除旧索引
     * </p>
     */
    public void rebuildIndexWithZeroDowntime() {
        String aliasName = getIndexName();
        String newIndexName = aliasName + "_v" + System.currentTimeMillis();
        
        try {
            log.info("[零停机重建] 开始重建索引: {} -> {}", aliasName, newIndexName);
            
            // 步骤 1：创建新索引
            createNewIndex(newIndexName);
            log.info("[零停机重建] 新索引创建成功: {}", newIndexName);
            
            // 步骤 2：从旧索引迁移数据
            if (indexExists(aliasName)) {
                long migratedCount = reindexData(aliasName, newIndexName);
                log.info("[零停机重建] 数据迁移完成: {} 条文档", migratedCount);
            } else {
                log.warn("[零停机重建] 旧索引不存在，跳过数据迁移");
            }
            
            // 步骤 3：原子切换别名
            switchAlias(aliasName, newIndexName);
            log.info("[零停机重建] 别名切换成功: {} -> {}", aliasName, newIndexName);
            
            // 步骤 4：删除旧索引（延迟执行，避免误操作）
            deleteOldIndex(aliasName);
            
            log.info("[零停机重建] 索引重建完成");
            
        } catch (IOException ex) {
            log.error("[零停机重建] 索引重建失败", ex);
            throw new IllegalStateException("Failed to rebuild index with zero downtime", ex);
        }
    }

    /**
     * 创建新索引
     */
    private void createNewIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", searchProperties.getIndexShards())
                .put("index.number_of_replicas", searchProperties.getIndexReplicas()));
        request.mapping(buildMapping());
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 从旧索引迁移数据到新索引
     */
    private long reindexData(String sourceIndex, String destIndex) throws IOException {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(sourceIndex);
        reindexRequest.setDestIndex(destIndex);
        // ES 7.17.x 使用 setSource().setSize() 设置批量大小
        reindexRequest.setConflicts("proceed");  // 忽略版本冲突
        
        BulkByScrollResponse response = client.reindex(reindexRequest, RequestOptions.DEFAULT);
        return response.getUpdated();
    }

    /**
     * 原子切换别名
     */
    private void switchAlias(String aliasName, String newIndexName) throws IOException {
        IndicesAliasesRequest aliasRequest = new IndicesAliasesRequest();
        
        // 如果别名已存在，先移除旧绑定
        if (indexExists(aliasName)) {
            aliasRequest.addAliasAction(
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(aliasName)
                    .alias(aliasName)
            );
        }
        
        // 添加新绑定
        aliasRequest.addAliasAction(
            new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(newIndexName)
                .alias(aliasName)
        );
        
        client.indices().updateAliases(aliasRequest, RequestOptions.DEFAULT);
    }

    /**
     * 删除旧索引
     */
    private void deleteOldIndex(String oldIndexName) {
        try {
            // 查找所有以该别名开头的旧索引
            GetIndexRequest getRequest = new GetIndexRequest(oldIndexName + "_*");
            String[] indices = client.indices().get(getRequest, RequestOptions.DEFAULT).getIndices();
            
            for (String index : indices) {
                if (!index.equals(oldIndexName)) {  // 保护当前别名指向的索引
                    AcknowledgedResponse response = client.indices()
                        .delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
                    if (response.isAcknowledged()) {
                        log.info("[零停机重建] 旧索引删除成功: {}", index);
                    }
                }
            }
        } catch (IOException ex) {
            log.warn("[零停机重建] 删除旧索引失败: {}", oldIndexName, ex);
        }
    }

    /**
     * 检查索引是否存在
     */
    private boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 简单重建索引（开发环境用，有停机时间）
     * 
     * @deprecated 生产环境请使用 {@link #rebuildIndexWithZeroDowntime()}
     */
    @Deprecated
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
            
            // Completion Suggester 字段（用于高性能自动补全）
            completionSuggest(builder, "nameSuggest", "name");
            completionSuggest(builder, "brandSuggest", "brandName");
            completionSuggest(builder, "categorySuggest", "categoryName");
            
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

    /**
     * 创建 Completion Suggester 字段
     * <p>
     * 用于高性能前缀匹配（自动补全）
     * </p>
     *
     * @param builder   XContentBuilder
     * @param fieldName 字段名（如 nameSuggest）
     * @param inputField 输入字段名（如 name）
     */
    private void completionSuggest(XContentBuilder builder, String fieldName, String inputField) throws IOException {
        builder.startObject(fieldName);
        builder.field("type", "completion");
        builder.field("analyzer", "simple");  // 使用简单分词器
        builder.field("search_analyzer", "simple");
        builder.field("max_input_length", 50);  // 最大输入长度
        builder.endObject();
    }
}
