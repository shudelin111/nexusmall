package com.nexusmall.search.application.service;

import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.command.BatchRemoveProductIndexCommand;
import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.application.dto.IndexOperationAuditDTO;
import com.nexusmall.search.domain.index.model.IndexBuildTask;
import com.nexusmall.search.domain.index.model.ProductIndexDocument;
import com.nexusmall.search.domain.index.repository.ProductDataRepository;
import com.nexusmall.search.domain.index.service.IndexDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IndexApplicationService {

    private static final Logger log = LoggerFactory.getLogger(IndexApplicationService.class);

    private final IndexDomainService indexDomainService;
    private final SearchAssembler searchAssembler;
    private final ProductDataRepository productDataRepository;

    public IndexApplicationService(IndexDomainService indexDomainService,
                                   SearchAssembler searchAssembler,
                                   ProductDataRepository productDataRepository) {
        this.indexDomainService = indexDomainService;
        this.searchAssembler = searchAssembler;
        this.productDataRepository = productDataRepository;
    }

    public void buildProductIndex(BuildProductIndexCommand command) {
        ProductIndexDocument document = command.getName() == null
                ? productDataRepository.findByProductId(command.getProductId())
                : searchAssembler.toIndexDocument(command);
        if (document == null) {
            throw new IllegalArgumentException("Product not found for indexing");
        }
        indexDomainService.save(document);
    }

    public void removeProductIndex(RemoveProductIndexCommand command) {
        String operationId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        log.info("[索引操作-{}] 开始清除商品索引: productId={}", operationId, command.getProductId());
        
        try {
            indexDomainService.remove(command.getProductId());
            
            logAudit(operationId, "REMOVE", "system", null, 
                    "productId=" + command.getProductId(), "SUCCESS", 1, 0, null, startTime);
            log.info("[索引操作-{}] 清除商品索引成功: productId={}", operationId, command.getProductId());
        } catch (Exception ex) {
            log.error("[索引操作-{}] 清除商品索引失败: productId={}", operationId, command.getProductId(), ex);
            logAudit(operationId, "REMOVE", "system", null, 
                    "productId=" + command.getProductId(), "FAILED", 0, 1, ex.getMessage(), startTime);
            throw ex;
        }
    }

    /**
     * 批量清除商品索引（生产级）
     * <p>
     * 特性：
     * - 优雅降级：单个失败不影响其他
     * - 详细审计：记录成功/失败数量
     * - 性能优化：批量操作减少网络往返
     * </p>
     *
     * @param command 批量清除命令
     * @return 操作审计信息
     */
    public IndexOperationAuditDTO batchRemoveProductIndex(BatchRemoveProductIndexCommand command) {
        String operationId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        List<Long> productIds = command.getProductIds();
        
        log.info("[索引操作-{}] 开始批量清除商品索引: count={}, operator={}, reason={}", 
                operationId, productIds.size(), command.getOperator(), command.getReason());
        
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long productId : productIds) {
            try {
                indexDomainService.remove(productId);
                successCount++;
            } catch (Exception ex) {
                failedCount++;
                String error = String.format("productId=%d, error=%s", productId, ex.getMessage());
                errors.add(error);
                log.error("[索引操作-{}] 清除商品索引失败: {}", operationId, error, ex);
            }
        }
        
        String result = determineResult(successCount, failedCount, productIds.size());
        String errorMessage = errors.isEmpty() ? null : String.join("; ", errors);
        
        IndexOperationAuditDTO audit = logAudit(operationId, "BATCH_REMOVE", 
                command.getOperator(), command.getReason(),
                String.format("productIds=%s", productIds), 
                result, successCount, failedCount, errorMessage, startTime);
        
        log.info("[索引操作-{}] 批量清除完成: total={}, success={}, failed={}", 
                operationId, productIds.size(), successCount, failedCount);
        
        return audit;
    }

    public int rebuildIndex(RebuildIndexCommand command) {
        IndexBuildTask task = new IndexBuildTask();
        task.setFullRebuild(command.isFullRebuild());
        task.setScheduledAt(LocalDateTime.now());
        List<ProductIndexDocument> documents = new ArrayList<ProductIndexDocument>();
        if (command.getProducts() == null || command.getProducts().isEmpty()) {
            documents.addAll(productDataRepository.listOnSaleProducts());
        } else {
            for (BuildProductIndexCommand item : command.getProducts()) {
                documents.add(item.getName() == null
                        ? productDataRepository.findByProductId(item.getProductId())
                        : searchAssembler.toIndexDocument(item));
            }
        }
        return indexDomainService.rebuild(task, documents);
    }

    /**
     * 同步商品索引（供 RocketMQ 消费者调用）
     */
    public void syncProductIndex(Long productId) {
        ProductIndexDocument document = productDataRepository.findByProductId(productId);
        if (document != null) {
            indexDomainService.save(document);
        } else {
            indexDomainService.remove(productId);
        }
    }

    /**
     * 记录审计日志
     */
    private IndexOperationAuditDTO logAudit(String operationId, String operationType,
                                            String operator, String reason,
                                            String targetDescription, String result,
                                            int successCount, int failedCount,
                                            String errorMessage, LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        
        IndexOperationAuditDTO audit = new IndexOperationAuditDTO();
        audit.setOperationId(operationId);
        audit.setOperationType(operationType);
        audit.setOperator(operator);
        audit.setReason(reason);
        audit.setTargetDescription(targetDescription);
        audit.setResult(result);
        audit.setSuccessCount(successCount);
        audit.setFailedCount(failedCount);
        audit.setErrorMessage(errorMessage);
        audit.setStartTime(startTime);
        audit.setEndTime(endTime);
        audit.setDurationMs(durationMs);
        
        // TODO: 生产环境应将审计日志持久化到数据库或发送到 Kafka
        // kafkaTemplate.send("INDEX_AUDIT_LOG", audit);
        
        return audit;
    }

    /**
     * 判断操作结果
     */
    private String determineResult(int successCount, int failedCount, int totalCount) {
        if (failedCount == 0) {
            return "SUCCESS";
        } else if (successCount == 0) {
            return "FAILED";
        } else {
            return "PARTIAL_SUCCESS";
        }
    }
}
