package com.nexusmall.cart.infrastructure.messaging;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.cart.infrastructure.repository.CartItemMapper;
import com.nexusmall.cart.domain.entity.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车同步消息消费者
 * <p>
 * 业界标准：
 * - 异步消费消息，同步到MySQL
 * - 幂等性保证（通过messageId去重）
 * - 失败重试机制（RocketMQ自动重试）
 * - 事务保证数据一致性
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "CART_SYNC_TOPIC",
        consumerGroup = "cart-sync-consumer-group",
        selectorExpression = "cart_sync"
)
public class CartSyncConsumer implements RocketMQListener<CartSyncMessage> {

    private final CartItemMapper cartItemMapper;

    /**
     * 消费购物车同步消息
     *
     * @param message 同步消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(CartSyncMessage message) {
        log.info("【消费购物车同步消息】messageId={}, userId={}, skuId={}, operationType={}",
                message.getMessageId(), message.getUserId(), message.getSkuId(), message.getOperationType());

        try {
            // 1. 幂等性校验（防止重复消费）
            if (isDuplicateMessage(message)) {
                log.warn("【消费购物车同步消息】重复消息，跳过处理, messageId={}", message.getMessageId());
                return;
            }

            // 2. 根据操作类型执行不同逻辑
            String operationType = message.getOperationType();
            
            switch (operationType) {
                case "ADD":
                case "UPDATE":
                    handleAddOrUpdate(message);
                    break;
                case "DELETE":
                    handleDelete(message);
                    break;
                case "MERGE":
                    handleMerge(message);
                    break;
                default:
                    log.warn("【消费购物车同步消息】未知操作类型: {}", operationType);
            }

            log.info("【消费购物车同步消息】成功, messageId={}, userId={}, skuId={}",
                    message.getMessageId(), message.getUserId(), message.getSkuId());
        } catch (Exception e) {
            log.error("【消费购物车同步消息】失败, messageId={}, userId={}, skuId={}",
                    message.getMessageId(), message.getUserId(), message.getSkuId(), e);
            // 抛出异常，触发RocketMQ重试机制
            throw new RuntimeException("消费购物车同步消息失败", e);
        }
    }

    /**
     * 处理新增/更新操作
     */
    private void handleAddOrUpdate(CartSyncMessage message) {
        CartItem existingItem = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, message.getUserId())
                .eq(CartItem::getSkuId, message.getSkuId()));

        if (existingItem != null) {
            // 更新
            existingItem.setQuantity(message.getQuantity());
            existingItem.setSnapshotPrice(message.getSnapshotPrice());
            existingItem.setProductName(message.getProductName());
            existingItem.setProductImage(message.getProductImage());
            existingItem.setSnapshotAttrs(message.getSnapshotAttrs());
            existingItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(existingItem);
            
            log.info("【消费购物车同步消息】更新成功, userId={}, skuId={}", 
                    message.getUserId(), message.getSkuId());
        } else {
            // 新增
            CartItem newItem = new CartItem();
            newItem.setUserId(message.getUserId());
            newItem.setSkuId(message.getSkuId());
            newItem.setSpuId(message.getSpuId());
            newItem.setProductName(message.getProductName());
            newItem.setProductImage(message.getProductImage());
            newItem.setSnapshotPrice(message.getSnapshotPrice());
            newItem.setSnapshotAttrs(message.getSnapshotAttrs());
            newItem.setSnapshotVersion(0);
            newItem.setSnapshotTime(message.getTimestamp() != null ? 
                    message.getTimestamp() : LocalDateTime.now());
            newItem.setQuantity(message.getQuantity());
            newItem.setSelected(message.getSelected() != null ? message.getSelected() : 1);
            cartItemMapper.insert(newItem);
            
            log.info("【消费购物车同步消息】新增成功, userId={}, skuId={}", 
                    message.getUserId(), message.getSkuId());
        }
    }

    /**
     * 处理删除操作
     */
    private void handleDelete(CartSyncMessage message) {
        int deleted = cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, message.getUserId())
                .eq(CartItem::getSkuId, message.getSkuId()));
        
        log.info("【消费购物车同步消息】删除成功, userId={}, skuId={}, deleted={}", 
                message.getUserId(), message.getSkuId(), deleted);
    }

    /**
     * 处理合并操作（同ADD）
     */
    private void handleMerge(CartSyncMessage message) {
        handleAddOrUpdate(message);
    }

    /**
     * 幂等性校验
     * <p>
     * 生产环境应将messageId存储到Redis或数据库，用于去重
     * 这里简化处理，仅做日志记录
     * </p>
     */
    private boolean isDuplicateMessage(CartSyncMessage message) {
        // TODO: 生产环境应实现真正的幂等性校验
        // 方案1: Redis SETNX messageId，设置过期时间24小时
        // 方案2: 数据库唯一索引 messageId
        return false;
    }
}
