package com.nexusmall.behavior.service.impl;

import cn.hutool.core.util.IdUtil;
import com.nexusmall.behavior.entity.UserBehaviorEsLog;
import com.nexusmall.behavior.service.UserBehaviorEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户行为日志 Elasticsearch Service 实现类
 * 
 * @author NexusMall
 * @since 2026-03-25
 */
@Slf4j
@Service
public class UserBehaviorEsServiceImpl implements UserBehaviorEsService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Override
    public boolean save(UserBehaviorEsLog esLog) {
        try {
            // 生成唯一 ID
            String uniqueId = esLog.getUserId() + "_" + 
                             System.currentTimeMillis() + "_" + 
                             IdUtil.fastSimpleUUID().substring(0, 8);
            esLog.setId(uniqueId);
            
            // 构建索引查询对象
            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(uniqueId)
                    .withObject(esLog)
                    .build();
            
            // 保存到 ES
            String documentId = elasticsearchTemplate.index(indexQuery, 
                    elasticsearchTemplate.getIndexCoordinatesFor(UserBehaviorEsLog.class));
            
            log.info("【用户行为保存至 ES】userId: {}, behaviorType: {}, documentId: {}", 
                    esLog.getUserId(), esLog.getBehaviorType(), documentId);
            
            return documentId != null;
        } catch (Exception e) {
            log.error("【用户行为保存至 ES 失败】userId: {}, behaviorType: {}", 
                    esLog.getUserId(), esLog.getBehaviorType(), e);
            return false;
        }
    }

    @Override
    public boolean saveFromVO(Long userId, String behaviorType, Long objectId,
                              String objectType, String extraData, String ipAddress,
                              String userAgent, LocalDateTime occurTime) {
        UserBehaviorEsLog esLog = UserBehaviorEsLog.builder()
                .userId(userId)
                .behaviorType(behaviorType)
                .objectId(objectId)
                .objectType(objectType)
                .extraData(extraData)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .occurTime(occurTime)
                .createTime(LocalDateTime.now())
                .build();
        
        return save(esLog);
    }

    /**
     * 初始化索引模板（可选）
     */
    public void initIndexTemplate() {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(UserBehaviorEsLog.class);
            if (!indexOps.exists()) {
                indexOps.create();
                log.info("【ES 索引初始化成功】");
            }
        } catch (Exception e) {
            log.error("【ES 索引初始化失败】", e);
        }
    }
}
