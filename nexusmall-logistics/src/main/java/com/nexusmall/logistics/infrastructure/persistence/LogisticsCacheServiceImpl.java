package com.nexusmall.logistics.infrastructure.persistence;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.logistics.config.LogisticsCacheConfig;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsFreightTemplateMapper;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsWarehouseMapper;
import com.nexusmall.logistics.application.service.LogisticsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 物流缓存服务实现�?
 * <p>
 * 业界标准�?
 * - Cache-Aside模式（旁路缓存）
 * - 先查缓存，命中则返回；未命中则查数据库并写入缓存
 * - 更新时删除缓存，保证数据一致�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsCacheServiceImpl implements LogisticsCacheService {

    private final RedissonClient redissonClient;
    private final LogisticsWarehouseMapper warehouseMapper;
    private final LogisticsFreightTemplateMapper freightTemplateMapper;
    private final LogisticsCacheConfig cacheConfig;

    @Override
    public LogisticsWarehouse getWarehouseWithCache(Long id) {
        if (!cacheConfig.isEnabled() || id == null) {
            return warehouseMapper.selectById(id);
        }

        String key = cacheConfig.getWarehouse().getKeyPrefix() + "id:" + id;
        
        try {
            // 1. 尝试从缓存获�?
            RBucket<String> bucket = redissonClient.getBucket(key);
            String cachedValue = bucket.get();
            
            if (cachedValue != null) {
                log.debug("【仓库缓存命中】id={}", id);
                return JSON.parseObject(cachedValue, LogisticsWarehouse.class);
            }

            // 2. 缓存未命中，查询数据�?
            log.debug("【仓库缓存未命中】id={}，查询数据库", id);
            LogisticsWarehouse warehouse = warehouseMapper.selectById(id);
            
            if (warehouse != null) {
                // 3. 写入缓存
                String jsonValue = JSON.toJSONString(warehouse);
                bucket.set(jsonValue, cacheConfig.getWarehouse().getTtlSeconds(), TimeUnit.SECONDS);
                log.debug("【仓库缓存写入】id={}, ttl={}s", id, cacheConfig.getWarehouse().getTtlSeconds());
            }
            
            return warehouse;
            
        } catch (Exception e) {
            log.error("【仓库缓存读取失败】id={}，降级查询数据库", id, e);
            // 缓存失败降级到数据库查询
            return warehouseMapper.selectById(id);
        }
    }

    @Override
    public LogisticsWarehouse getWarehouseByCodeWithCache(String warehouseCode) {
        if (!cacheConfig.isEnabled() || warehouseCode == null) {
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getWarehouseCode, warehouseCode);
            return warehouseMapper.selectOne(wrapper);
        }

        String key = cacheConfig.getWarehouse().getKeyPrefix() + "code:" + warehouseCode;
        
        try {
            // 1. 尝试从缓存获�?
            RBucket<String> bucket = redissonClient.getBucket(key);
            String cachedValue = bucket.get();
            
            if (cachedValue != null) {
                log.debug("【仓库缓存命中】code={}", warehouseCode);
                return JSON.parseObject(cachedValue, LogisticsWarehouse.class);
            }

            // 2. 缓存未命中，查询数据�?
            log.debug("【仓库缓存未命中】code={}，查询数据库", warehouseCode);
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getWarehouseCode, warehouseCode);
            LogisticsWarehouse warehouse = warehouseMapper.selectOne(wrapper);
            
            if (warehouse != null) {
                // 3. 写入缓存
                String jsonValue = JSON.toJSONString(warehouse);
                bucket.set(jsonValue, cacheConfig.getWarehouse().getTtlSeconds(), TimeUnit.SECONDS);
                log.debug("【仓库缓存写入】code={}, ttl={}s", warehouseCode, cacheConfig.getWarehouse().getTtlSeconds());
            }
            
            return warehouse;
            
        } catch (Exception e) {
            log.error("【仓库缓存读取失败】code={}，降级查询数据库", warehouseCode, e);
            // 缓存失败降级到数据库查询
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getWarehouseCode, warehouseCode);
            return warehouseMapper.selectOne(wrapper);
        }
    }

    @Override
    public List<LogisticsWarehouse> listEnabledWarehousesWithCache() {
        if (!cacheConfig.isEnabled()) {
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getStatus, 1);
            return warehouseMapper.selectList(wrapper);
        }

        String key = cacheConfig.getWarehouse().getKeyPrefix() + "list:enabled";
        
        try {
            // 1. 尝试从缓存获�?
            RBucket<String> bucket = redissonClient.getBucket(key);
            String cachedValue = bucket.get();
            
            if (cachedValue != null) {
                log.debug("【仓库列表缓存命中�?);
                return JSON.parseArray(cachedValue, LogisticsWarehouse.class);
            }

            // 2. 缓存未命中，查询数据�?
            log.debug("【仓库列表缓存未命中】，查询数据�?);
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getStatus, 1);
            List<LogisticsWarehouse> warehouses = warehouseMapper.selectList(wrapper);
            
            if (warehouses != null && !warehouses.isEmpty()) {
                // 3. 写入缓存
                String jsonValue = JSON.toJSONString(warehouses);
                bucket.set(jsonValue, cacheConfig.getWarehouse().getListTtlSeconds(), TimeUnit.SECONDS);
                log.debug("【仓库列表缓存写入】count={}, ttl={}s", 
                        warehouses.size(), cacheConfig.getWarehouse().getListTtlSeconds());
            }
            
            return warehouses != null ? warehouses : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("【仓库列表缓存读取失败】，降级查询数据�?, e);
            // 缓存失败降级到数据库查询
            LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsWarehouse::getStatus, 1);
            List<LogisticsWarehouse> warehouses = warehouseMapper.selectList(wrapper);
            return warehouses != null ? warehouses : Collections.emptyList();
        }
    }

    @Override
    public void evictWarehouseCache(Long id) {
        if (!cacheConfig.isEnabled() || id == null) {
            return;
        }

        try {
            // 删除单个仓库缓存
            String keyById = cacheConfig.getWarehouse().getKeyPrefix() + "id:" + id;
            redissonClient.getBucket(keyById).delete();
            
            // 删除仓库列表缓存（因为列表可能包含该仓库�?
            String keyList = cacheConfig.getWarehouse().getKeyPrefix() + "list:enabled";
            redissonClient.getBucket(keyList).delete();
            
            log.info("【清除仓库缓存】id={}", id);
        } catch (Exception e) {
            log.error("【清除仓库缓存失败】id={}", id, e);
        }
    }

    @Override
    public void evictAllWarehouseCache() {
        if (!cacheConfig.isEnabled()) {
            return;
        }

        try {
            // 使用模式匹配删除所有仓库相关缓�?
            String pattern = cacheConfig.getWarehouse().getKeyPrefix() + "*";
            redissonClient.getKeys().deleteByPattern(pattern);
            
            log.info("【清除所有仓库缓存�?);
        } catch (Exception e) {
            log.error("【清除所有仓库缓存失败�?, e);
        }
    }

    @Override
    public LogisticsFreightTemplate getFreightTemplateWithCache(Long id) {
        if (!cacheConfig.isEnabled() || id == null) {
            return freightTemplateMapper.selectById(id);
        }

        String key = cacheConfig.getFreightTemplate().getKeyPrefix() + "id:" + id;
        
        try {
            // 1. 尝试从缓存获�?
            RBucket<String> bucket = redissonClient.getBucket(key);
            String cachedValue = bucket.get();
            
            if (cachedValue != null) {
                log.debug("【运费模板缓存命中】id={}", id);
                return JSON.parseObject(cachedValue, LogisticsFreightTemplate.class);
            }

            // 2. 缓存未命中，查询数据�?
            log.debug("【运费模板缓存未命中】id={}，查询数据库", id);
            LogisticsFreightTemplate template = freightTemplateMapper.selectById(id);
            
            if (template != null) {
                // 3. 写入缓存
                String jsonValue = JSON.toJSONString(template);
                bucket.set(jsonValue, cacheConfig.getFreightTemplate().getTtlSeconds(), TimeUnit.SECONDS);
                log.debug("【运费模板缓存写入】id={}, ttl={}s", id, cacheConfig.getFreightTemplate().getTtlSeconds());
            }
            
            return template;
            
        } catch (Exception e) {
            log.error("【运费模板缓存读取失败】id={}，降级查询数据库", id, e);
            // 缓存失败降级到数据库查询
            return freightTemplateMapper.selectById(id);
        }
    }

    @Override
    public LogisticsFreightTemplate getDefaultFreightTemplateWithCache() {
        if (!cacheConfig.isEnabled()) {
            LambdaQueryWrapper<LogisticsFreightTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsFreightTemplate::getIsDefault, 1);
            wrapper.last("LIMIT 1");
            return freightTemplateMapper.selectOne(wrapper);
        }

        String key = cacheConfig.getFreightTemplate().getKeyPrefix() + "default";
        
        try {
            // 1. 尝试从缓存获�?
            RBucket<String> bucket = redissonClient.getBucket(key);
            String cachedValue = bucket.get();
            
            if (cachedValue != null) {
                log.debug("【默认运费模板缓存命中�?);
                return JSON.parseObject(cachedValue, LogisticsFreightTemplate.class);
            }

            // 2. 缓存未命中，查询数据�?
            log.debug("【默认运费模板缓存未命中】，查询数据�?);
            LambdaQueryWrapper<LogisticsFreightTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsFreightTemplate::getIsDefault, 1);
            wrapper.last("LIMIT 1");
            LogisticsFreightTemplate template = freightTemplateMapper.selectOne(wrapper);
            
            if (template != null) {
                // 3. 写入缓存（更长的TTL，因为访问频率高�?
                String jsonValue = JSON.toJSONString(template);
                bucket.set(jsonValue, cacheConfig.getFreightTemplate().getDefaultTtlSeconds(), TimeUnit.SECONDS);
                log.debug("【默认运费模板缓存写入】ttl={}s", cacheConfig.getFreightTemplate().getDefaultTtlSeconds());
            }
            
            return template;
            
        } catch (Exception e) {
            log.error("【默认运费模板缓存读取失败】，降级查询数据�?, e);
            // 缓存失败降级到数据库查询
            LambdaQueryWrapper<LogisticsFreightTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsFreightTemplate::getIsDefault, 1);
            wrapper.last("LIMIT 1");
            return freightTemplateMapper.selectOne(wrapper);
        }
    }

    @Override
    public void evictFreightTemplateCache(Long id) {
        if (!cacheConfig.isEnabled() || id == null) {
            return;
        }

        try {
            // 删除单个模板缓存
            String keyById = cacheConfig.getFreightTemplate().getKeyPrefix() + "id:" + id;
            redissonClient.getBucket(keyById).delete();
            
            // 如果是默认模板，也删除默认模板缓�?
            LogisticsFreightTemplate template = freightTemplateMapper.selectById(id);
            if (template != null && template.getIsDefault() != null && template.getIsDefault() == 1) {
                String keyDefault = cacheConfig.getFreightTemplate().getKeyPrefix() + "default";
                redissonClient.getBucket(keyDefault).delete();
                log.info("【清除默认运费模板缓存�?);
            }
            
            log.info("【清除运费模板缓存】id={}", id);
        } catch (Exception e) {
            log.error("【清除运费模板缓存失败】id={}", id, e);
        }
    }

    @Override
    public void evictAllFreightTemplateCache() {
        if (!cacheConfig.isEnabled()) {
            return;
        }

        try {
            // 使用模式匹配删除所有运费模板相关缓�?
            String pattern = cacheConfig.getFreightTemplate().getKeyPrefix() + "*";
            redissonClient.getKeys().deleteByPattern(pattern);
            
            log.info("【清除所有运费模板缓存�?);
        } catch (Exception e) {
            log.error("【清除所有运费模板缓存失败�?, e);
        }
    }
}
