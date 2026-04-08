package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.command.BatchRemoveProductIndexCommand;
import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.application.dto.IndexOperationAuditDTO;
import com.nexusmall.search.application.service.IndexApplicationService;
import com.nexusmall.search.infrastructure.persistence.elasticsearch.support.ProductIndexSchemaManager;
import com.nexusmall.search.interfaces.dto.BatchRemoveIndexRequest;
import com.nexusmall.search.interfaces.dto.IndexProductRequest;
import com.nexusmall.search.interfaces.dto.RebuildIndexRequest;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引管理控制器
 * <p>
 * 提供索引重建、商品同步等管理功能
 * </p>
 */
@Tag(name = "索引管理", description = "Elasticsearch 索引管理接口")
@Validated
@RestController
@RequestMapping("/index")  // 索引管理子路径
public class IndexController {

    private final SearchFacade searchFacade;
    private final ProductIndexSchemaManager schemaManager;
    private final IndexApplicationService indexApplicationService;

    public IndexController(SearchFacade searchFacade, 
                          ProductIndexSchemaManager schemaManager,
                          IndexApplicationService indexApplicationService) {
        this.searchFacade = searchFacade;
        this.schemaManager = schemaManager;
        this.indexApplicationService = indexApplicationService;
    }

    @PostMapping(value = "/products", headers = "X-API-Version=v1")
    public Result<Void> buildIndex(@Validated @RequestBody IndexProductRequest request) {
        searchFacade.buildIndex(toBuildCommand(request));
        return Result.success();
    }

    /**
     * 零停机重建索引（生产环境标准）
     * <p>
     * 流程：
     * 1. 创建新索引（带时间戳后缀）
     * 2. 从旧索引迁移数据
     * 3. 原子切换别名（用户无感知）
     * 4. 删除旧索引
     * </p>
     *
     * @return 操作结果
     */
    @Operation(
        summary = "零停机重建索引",
        description = "生产环境标准：创建新索引 → 迁移数据 → 切换别名 → 删除旧索引，全程服务不中断"
    )
    @PostMapping(value = "/rebuild-zero-downtime", headers = "X-API-Version=v1")
    public Result<String> rebuildIndexZeroDowntime() {
        try {
            schemaManager.rebuildIndexWithZeroDowntime();
            return Result.success("索引重建成功（零停机）");
        } catch (Exception ex) {
            return Result.failure("INDEX_REBUILD_FAILED", "索引重建失败: " + ex.getMessage());
        }
    }

    /**
     * 简单重建索引（开发环境用，有停机时间）
     * 
     * @deprecated 生产环境请使用 /index/rebuild-zero-downtime
     */
    @Deprecated
    @Operation(
        summary = "简单重建索引",
        description = "开发环境用：删除旧索引 → 创建新索引，期间服务不可用"
    )
    @PostMapping(value = "/rebuild-simple", headers = "X-API-Version=v1")
    public Result<String> rebuildIndexSimple() {
        try {
            schemaManager.recreateIndex();
            return Result.success("索引重建成功（简单模式）");
        } catch (Exception ex) {
            return Result.failure("INDEX_REBUILD_FAILED", "索引重建失败: " + ex.getMessage());
        }
    }

    @DeleteMapping(value = "/products/{productId}", headers = "X-API-Version=v1")
    public Result<Void> deleteIndex(@PathVariable("productId") Long productId) {
        RemoveProductIndexCommand command = new RemoveProductIndexCommand();
        command.setProductId(productId);
        searchFacade.removeIndex(command);
        return Result.success();
    }

    /**
     * 批量清除商品索引（生产级）
     * <p>
     * 特性：
     * - 优雅降级：单个失败不影响其他
     * - 审计日志：记录操作人、原因、结果
     * - 详细返回：成功/失败数量
     * </p>
     *
     * @param request 批量清除请求
     * @return 操作审计信息
     */
    @Operation(
        summary = "批量清除商品索引",
        description = "生产级：支持批量操作，优雅降级，完整审计日志"
    )
    @PostMapping(value = "/products/batch-remove", headers = "X-API-Version=v1")
    public Result<IndexOperationAuditDTO> batchRemoveIndex(@Validated @RequestBody BatchRemoveIndexRequest request) {
        BatchRemoveProductIndexCommand command = new BatchRemoveProductIndexCommand();
        command.setProductIds(request.getProductIds());
        command.setOperator(request.getOperator());
        command.setReason(request.getReason());
        
        IndexOperationAuditDTO audit = indexApplicationService.batchRemoveProductIndex(command);
        return Result.success(audit);
    }

    private BuildProductIndexCommand toBuildCommand(IndexProductRequest request) {
        BuildProductIndexCommand command = new BuildProductIndexCommand();
        BeanUtils.copyProperties(request, command);
        return command;
    }
}
