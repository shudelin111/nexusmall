package com.nexusmall.search.application.command;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量清除商品索引命令
 * <p>
 * 生产级特性：
 * - 支持批量操作（减少网络往返）
 * - 参数校验（防止空列表）
 * - 审计日志追踪
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-08
 */
public class BatchRemoveProductIndexCommand {

    /**
     * 商品 ID 列表
     */
    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> productIds;

    /**
     * 操作人（用于审计日志）
     */
    private String operator;

    /**
     * 操作原因（用于审计日志）
     */
    private String reason;

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
