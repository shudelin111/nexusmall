package com.nexusmall.inventory.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.inventory.application.service.SkuStockService;
import com.nexusmall.inventory.domain.entity.SkuStock;
import com.nexusmall.inventory.infrastructure.persistence.mapper.SkuStockMapper;
import com.nexusmall.inventory.interfaces.dto.SkuStockVO;
import com.nexusmall.inventory.interfaces.dto.StockDeductRequest;
import com.nexusmall.inventory.interfaces.dto.StockRollbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * SKU库存服务实现�?
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {

    private final RedissonClient redissonClient;
    private final SkuStockMapper skuStockMapper;

    @Override
    public SkuStockVO getSkuStock(Long skuId, Long warehouseId) {
        log.info("查询SKU库存: skuId={}, warehouseId={}", skuId, warehouseId);
        
        LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuStock::getSkuId, skuId)
               .eq(SkuStock::getWarehouseId, warehouseId);
        
        SkuStock stock = this.getOne(wrapper);
        if (stock == null) {
            return null;
        }
        
        SkuStockVO vo = new SkuStockVO();
        vo.setId(stock.getId());
        vo.setSkuId(stock.getSkuId());
        vo.setWarehouseId(stock.getWarehouseId());
        vo.setStock(stock.getStock());
        vo.setLockedStock(stock.getLockedStock());
        vo.setActualStock(stock.getActualStock());
        vo.setWarningThreshold(stock.getWarningThreshold());
        vo.setLowStock(stock.getStock() != null && stock.getWarningThreshold() != null 
                && stock.getStock() <= stock.getWarningThreshold());
        vo.setCreateTime(stock.getCreateTime());
        vo.setUpdateTime(stock.getUpdateTime());
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(StockDeductRequest request) {
        String lockKey = "stock:deduct:" + request.getSkuId() + ":" + request.getWarehouseId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 获取分布式锁，最多等�?秒，锁定10秒后自动释放
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.error("获取库存锁失�? {}", lockKey);
                return false;
            }
            
            log.info("扣减库存: skuId={}, quantity={}, businessSn={}", 
                    request.getSkuId(), request.getQuantity(), request.getBusinessSn());
            
            LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuStock::getSkuId, request.getSkuId())
                   .eq(SkuStock::getWarehouseId, request.getWarehouseId());
            
            SkuStock stock = this.getOne(wrapper);
            if (stock == null || stock.getStock() < request.getQuantity()) {
                log.warn("库存不足: skuId={}, warehouseId={}, quantity={}", 
                        request.getSkuId(), request.getWarehouseId(), request.getQuantity());
                return false;
            }
            
            // 使用乐观锁扣减库�?
            int rows = skuStockMapper.deductStock(
                    request.getSkuId(), 
                    request.getWarehouseId(), 
                    request.getQuantity(), 
                    stock.getVersion()
            );
            
            boolean success = rows > 0;
            log.info("库存扣减{}", success ? "成功" : "失败（版本冲突）");
            return success;
            
        } catch (InterruptedException e) {
            log.error("获取锁被中断: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(StockRollbackRequest request) {
        String lockKey = "stock:rollback:" + request.getSkuId() + ":" + request.getWarehouseId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.error("获取库存锁失�? {}", lockKey);
                return false;
            }
            
            log.info("回滚库存: skuId={}, quantity={}, businessSn={}", 
                    request.getSkuId(), request.getQuantity(), request.getBusinessSn());
            
            LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuStock::getSkuId, request.getSkuId())
                   .eq(SkuStock::getWarehouseId, request.getWarehouseId());
            
            SkuStock stock = this.getOne(wrapper);
            if (stock == null || stock.getLockedStock() < request.getQuantity()) {
                log.warn("回滚库存失败，锁定库存不�? skuId={}, warehouseId={}", 
                        request.getSkuId(), request.getWarehouseId());
                return false;
            }
            
            // 使用乐观锁回滚库�?
            int rows = skuStockMapper.rollbackStock(
                    request.getSkuId(), 
                    request.getWarehouseId(), 
                    request.getQuantity(), 
                    stock.getVersion()
            );
            
            boolean success = rows > 0;
            log.info("库存回滚{}", success ? "成功" : "失败（版本冲突）");
            return success;
            
        } catch (InterruptedException e) {
            log.error("获取锁被中断: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmStock(Long skuId, Long warehouseId, Integer quantity, String businessSn) {
        String lockKey = "stock:confirm:" + skuId + ":" + warehouseId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.error("获取库存锁失�? {}", lockKey);
                return false;
            }
            
            log.info("确认库存: skuId={}, quantity={}, businessSn={}", skuId, quantity, businessSn);
            
            LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuStock::getSkuId, skuId)
                   .eq(SkuStock::getWarehouseId, warehouseId);
            
            SkuStock stock = this.getOne(wrapper);
            if (stock == null || stock.getLockedStock() < quantity) {
                log.warn("确认库存失败，锁定库存不�? skuId={}, warehouseId={}", skuId, warehouseId);
                return false;
            }
            
            // 使用乐观锁确认库�?
            int rows = skuStockMapper.confirmStock(skuId, warehouseId, quantity, stock.getVersion());
            
            boolean success = rows > 0;
            log.info("库存确认{}", success ? "成功" : "失败（版本冲突）");
            return success;
            
        } catch (InterruptedException e) {
            log.error("获取锁被中断: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean checkStockSufficient(Long skuId, Long warehouseId, Integer quantity) {
        log.info("检查库�? skuId={}, warehouseId={}, quantity={}", skuId, warehouseId, quantity);
        
        LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuStock::getSkuId, skuId)
               .eq(SkuStock::getWarehouseId, warehouseId)
               .ge(SkuStock::getStock, quantity);
        
        long count = this.count(wrapper);
        return count > 0;
    }
}
