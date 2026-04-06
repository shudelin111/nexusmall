package com.nexusmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.cart.dao.CartItemMapper;
import com.nexusmall.cart.dto.AddCartRequest;
import com.nexusmall.cart.entity.CartItem;
import com.nexusmall.cart.exception.CartException;
import com.nexusmall.cart.feign.ProductFeignClient;
import com.nexusmall.cart.mq.CartSyncMessage;
import com.nexusmall.cart.mq.CartSyncProducer;
import com.nexusmall.cart.vo.CartItemVO;
import com.nexusmall.cart.vo.CartSummaryVO;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车核心服务（业界标准版）
 * <p>
 * 核心架构：
 * - Redis Hash 主存储（高性能读写，QPS > 10万）
 * - MySQL 异步持久化（数据兜底，RocketMQ解耦）
 * - 商品快照机制（防止商家改价导致体验问题）
 * - Redisson分布式锁（防止并发更新冲突）
 * - Feign批量查询（减少网络开销）
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CartItemMapper cartItemMapper;
    private final ProductFeignClient productFeignClient;
    private final RedissonClient redissonClient;
    private final CartSyncProducer cartSyncProducer;

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String LOCK_KEY_PREFIX = "lock:cart:user:";
    private static final int LOGGED_IN_EXPIRE_DAYS = 30;
    private static final int ANONYMOUS_EXPIRE_HOURS = 24;
    private static final int MAX_CART_ITEMS = 100; // 购物车最大商品种类数

    /**
     * 添加商品到购物车（增强版）
     * <p>
     * 核心特性：
     * 1. 幂等性保证 - 相同用户+SKU只更新数量
     * 2. 分布式锁 - 防止并发更新冲突
     * 3. 商品校验 - 调用Product服务验证商品有效性
     * 4. 库存检查 - 实时检查库存状态
     * 5. 限购检查 - 验证是否超过单次购买上限
     * 6. 购物车容量限制 - 最多100种商品
     * </p>
     *
     * @param request 添加购物车请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(AddCartRequest request) {
        Long userId = request.getUserId();
        Long skuId = request.getSkuId();
        Integer quantity = request.getQuantity();

        log.info("【添加购物车】userId={}, skuId={}, quantity={}", userId, skuId, quantity);

        // 1. 获取分布式锁（防止并发冲突）
        String lockKey = LOCK_KEY_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试加锁，最多等待3秒，锁定10秒后自动释放
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
            }

            // 2. 校验购物车容量
            validateCartCapacity(userId);

            // 3. 调用Product服务校验商品
            ProductFeignClient.SkuInfo skuInfo = validateAndFetchSkuInfo(skuId);

            // 4. 检查库存
            checkStock(skuId, quantity, skuInfo);

            // 5. 检查限购
            checkPurchaseLimit(userId, skuId, quantity, skuInfo);

            // 6. 执行添加逻辑
            executeAddToCart(userId, skuId, skuInfo, quantity, request.getAttrs());

            log.info("【添加购物车】成功, userId={}, skuId={}", userId, skuId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【添加购物车】加锁中断, userId={}, skuId={}", userId, skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统异常");
        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("【添加购物车】失败, userId={}, skuId={}", userId, skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "添加购物车失败: " + e.getMessage());
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 查询用户购物车汇总（增强版）
     * <p>
     * 核心特性：
     * 1. 批量查询商品信息 - 减少网络开销
     * 2. 实时价格对比 - 检测价格变动
     * 3. 库存状态检查 - 标记失效商品
     * 4. 统计信息计算 - 总价、优惠、数量
     * </p>
     *
     * @param userId 用户ID
     * @return 购物车汇总
     */
    public CartSummaryVO getCartSummary(Long userId) {
        log.info("【查询购物车汇总】userId={}", userId);

        try {
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);

            if (entries == null || entries.isEmpty()) {
                return buildEmptyCartSummary(userId);
            }

            // 1. 解析购物车项
            List<Map<String, Object>> rawItems = new ArrayList<>();
            List<Long> skuIds = new ArrayList<>();
            
            for (Object value : entries.values()) {
                Map<String, Object> item = JSON.parseObject(JSON.toJSONString(value), Map.class);
                rawItems.add(item);
                skuIds.add(Long.valueOf(item.get("skuId").toString()));
            }

            // 2. 批量查询商品信息（Feign调用）
            Map<Long, ProductFeignClient.SkuInfo> skuInfoMap = batchQuerySkuInfos(skuIds);

            // 3. 构建VO列表并分类
            List<CartItemVO> validItems = new ArrayList<>();
            List<CartItemVO> invalidItems = new ArrayList<>();
            List<String> tips = new ArrayList<>();

            for (Map<String, Object> rawItem : rawItems) {
                CartItemVO vo = convertToVO(rawItem, skuInfoMap);
                
                if (vo.getValid()) {
                    validItems.add(vo);
                    
                    // 收集提示信息
                    if (vo.getPriceChangeFlag() == 1) {
                        tips.add("「" + vo.getProductName() + "」价格上涨了");
                    } else if (vo.getPriceChangeFlag() == 2) {
                        tips.add("「" + vo.getProductName() + "」降价了，快来看看吧");
                    }
                } else {
                    invalidItems.add(vo);
                    tips.add("「" + vo.getProductName() + "」" + vo.getInvalidReason());
                }
            }

            // 4. 计算统计信息
            CartSummaryVO summary = calculateSummary(userId, validItems, invalidItems, tips);

            log.info("【查询购物车汇总】成功, userId={}, validCount={}, invalidCount={}", 
                    userId, validItems.size(), invalidItems.size());
            return summary;
        } catch (Exception e) {
            log.error("【查询购物车汇总】失败, userId={}", userId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "查询购物车失败: " + e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量（增强版）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        log.info("【更新数量】userId={}, skuId={}, quantity={}", userId, skuId, quantity);

        if (quantity <= 0) {
            throw new CartException(CommonResultCode.PARAM_INVALID, "数量必须大于0");
        }

        String lockKey = LOCK_KEY_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
            }

            String cartKey = CART_KEY_PREFIX + userId;
            String field = String.valueOf(skuId);

            // 1. 检查是否存在
            Boolean exists = redisTemplate.opsForHash().hasKey(cartKey, field);
            if (!Boolean.TRUE.equals(exists)) {
                throw new CartException(CommonResultCode.PARAM_INVALID, "商品不在购物车中");
            }

            // 2. 获取商品信息并检查库存
            ProductFeignClient.SkuInfo skuInfo = validateAndFetchSkuInfo(skuId);
            checkStock(skuId, quantity, skuInfo);
            checkPurchaseLimit(userId, skuId, quantity, skuInfo);

            // 3. 更新Redis
            Object existingValue = redisTemplate.opsForHash().get(cartKey, field);
            Map<String, Object> existingItem = JSON.parseObject(JSON.toJSONString(existingValue), Map.class);
            existingItem.put("quantity", quantity);
            redisTemplate.opsForHash().put(cartKey, field, existingItem);

            // 4. 异步同步MySQL
            syncToMySQL(userId, skuId, existingItem);

            log.info("【更新数量】成功, userId={}, skuId={}", userId, skuId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统异常");
        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("【更新数量】失败, userId={}, skuId={}", userId, skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "更新数量失败: " + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 删除购物车商品
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeFromCart(Long userId, Long skuId) {
        log.info("【删除购物车】userId={}, skuId={}", userId, skuId);

        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String field = String.valueOf(skuId);

            // 1. 从Redis删除
            redisTemplate.opsForHash().delete(cartKey, field);

            // 2. 从MySQL删除
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .eq(CartItem::getSkuId, skuId));

            log.info("【删除购物车】成功, userId={}, skuId={}", userId, skuId);
        } catch (Exception e) {
            log.error("【删除购物车】失败, userId={}, skuId={}", userId, skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "删除购物车商品失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除购物车商品
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveFromCart(Long userId, List<Long> skuIds) {
        log.info("【批量删除】userId={}, skuIds={}", userId, skuIds);

        try {
            String cartKey = CART_KEY_PREFIX + userId;

            // 1. 批量从Redis删除
            for (Long skuId : skuIds) {
                redisTemplate.opsForHash().delete(cartKey, String.valueOf(skuId));
            }

            // 2. 批量从MySQL删除
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getSkuId, skuIds));

            log.info("【批量删除】成功, userId={}, count={}", userId, skuIds.size());
        } catch (Exception e) {
            log.error("【批量删除】失败, userId={}, skuIds={}", userId, skuIds, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 清空购物车
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        log.info("【清空购物车】userId={}", userId);

        try {
            String cartKey = CART_KEY_PREFIX + userId;

            // 1. 删除Redis Key
            redisTemplate.delete(cartKey);

            // 2. 删除MySQL数据
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId));

            log.info("【清空购物车】成功, userId={}", userId);
        } catch (Exception e) {
            log.error("【清空购物车】失败, userId={}", userId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "清空购物车失败: " + e.getMessage());
        }
    }

    /**
     * 选中/取消选中商品
     */
    public void toggleSelected(Long userId, Long skuId, Integer selected) {
        log.info("【切换选中状态】userId={}, skuId={}, selected={}", userId, skuId, selected);

        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String field = String.valueOf(skuId);

            Object existingValue = redisTemplate.opsForHash().get(cartKey, field);
            if (existingValue == null) {
                throw new CartException(CommonResultCode.PARAM_INVALID, "商品不在购物车中");
            }

            Map<String, Object> item = JSON.parseObject(JSON.toJSONString(existingValue), Map.class);
            item.put("selected", selected);
            redisTemplate.opsForHash().put(cartKey, field, item);

            log.info("【切换选中状态】成功, userId={}, skuId={}", userId, skuId);
        } catch (Exception e) {
            log.error("【切换选中状态】失败, userId={}, skuId={}", userId, skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 全选/全不选
     */
    public void toggleSelectAll(Long userId, Integer selected) {
        log.info("【全选/全不选】userId={}, selected={}", userId, selected);

        try {
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);

            for (Object key : entries.keySet()) {
                Map<String, Object> item = JSON.parseObject(JSON.toJSONString(entries.get(key)), Map.class);
                item.put("selected", selected);
                redisTemplate.opsForHash().put(cartKey, key, item);
            }

            log.info("【全选/全不选】成功, userId={}", userId);
        } catch (Exception e) {
            log.error("【全选/全不选】失败, userId={}", userId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 合并匿名购物车到登录账户（业界标准）
     * <p>
     * 核心逻辑：
     * 1. 获取匿名用户购物车
     * 2. 遍历每个商品
     * 3. 相同SKU：数量累加（需检查限购）
     * 4. 不同SKU：直接追加
     * 5. 删除匿名用户购物车
     * </p>
     *
     * @param loggedInUserId 登录用户ID
     * @param anonymousUserId 匿名用户ID（临时ID）
     */
    @Transactional(rollbackFor = Exception.class)
    public void mergeCart(Long loggedInUserId, Long anonymousUserId) {
        log.info("【合并购物车】loggedInUserId={}, anonymousUserId={}", loggedInUserId, anonymousUserId);

        String lockKey = LOCK_KEY_PREFIX + loggedInUserId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
            }

            String anonymousCartKey = CART_KEY_PREFIX + anonymousUserId;
            String loggedInCartKey = CART_KEY_PREFIX + loggedInUserId;

            // 1. 获取匿名用户购物车
            Map<Object, Object> anonymousItems = redisTemplate.opsForHash().entries(anonymousCartKey);
            if (anonymousItems == null || anonymousItems.isEmpty()) {
                log.info("【合并购物车】匿名购物车为空，无需合并");
                return;
            }

            // 2. 获取登录用户购物车
            Map<Object, Object> loggedInItems = redisTemplate.opsForHash().entries(loggedInCartKey);
            Set<Long> existingSkuIds = new HashSet<>();
            if (loggedInItems != null && !loggedInItems.isEmpty()) {
                for (Object value : loggedInItems.values()) {
                    Map<String, Object> item = JSON.parseObject(JSON.toJSONString(value), Map.class);
                    existingSkuIds.add(Long.valueOf(item.get("skuId").toString()));
                }
            }

            // 3. 检查容量
            long currentSize = existingSkuIds.size();
            long newSize = anonymousItems.size();
            if (currentSize + newSize > MAX_CART_ITEMS) {
                throw new CartException(CommonResultCode.PARAM_INVALID,
                        "合并后超过购物车容量限制（最多" + MAX_CART_ITEMS + "种商品）");
            }

            // 4. 合并逻辑
            int mergedCount = 0;
            int addedCount = 0;

            for (Object value : anonymousItems.values()) {
                Map<String, Object> anonymousItem = JSON.parseObject(JSON.toJSONString(value), Map.class);
                Long skuId = Long.valueOf(anonymousItem.get("skuId").toString());
                Integer quantity = (Integer) anonymousItem.get("quantity");

                if (existingSkuIds.contains(skuId)) {
                    // 相同SKU：数量累加
                    Object existingValue = redisTemplate.opsForHash().get(loggedInCartKey, String.valueOf(skuId));
                    Map<String, Object> existingItem = JSON.parseObject(JSON.toJSONString(existingValue), Map.class);
                    Integer existingQty = (Integer) existingItem.get("quantity");
                    
                    // 检查限购
                    ProductFeignClient.SkuInfo skuInfo = validateAndFetchSkuInfo(skuId);
                    checkPurchaseLimit(loggedInUserId, skuId, existingQty + quantity, skuInfo);
                    
                    existingItem.put("quantity", existingQty + quantity);
                    redisTemplate.opsForHash().put(loggedInCartKey, String.valueOf(skuId), existingItem);
                    mergedCount++;
                    
                    log.info("【合并购物车】SKU {} 数量累加: {} -> {}", skuId, existingQty, existingQty + quantity);
                } else {
                    // 不同SKU：直接追加
                    redisTemplate.opsForHash().put(loggedInCartKey, String.valueOf(skuId), anonymousItem);
                    addedCount++;
                    
                    log.info("【合并购物车】新增 SKU {}", skuId);
                }
            }

            // 5. 设置过期时间
            redisTemplate.expire(loggedInCartKey, LOGGED_IN_EXPIRE_DAYS, TimeUnit.DAYS);

            // 6. 删除匿名用户购物车
            redisTemplate.delete(anonymousCartKey);

            // 7. 异步同步MySQL
            syncMergedCartToMySQL(loggedInUserId, loggedInCartKey);

            log.info("【合并购物车】成功, loggedInUserId={}, mergedCount={}, addedCount={}",
                    loggedInUserId, mergedCount, addedCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "系统异常");
        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("【合并购物车】失败, loggedInUserId={}, anonymousUserId={}",
                    loggedInUserId, anonymousUserId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "合并购物车失败: " + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ===============================
    // 私有方法
    // ===============================

    /**
     * 校验购物车容量
     */
    private void validateCartCapacity(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        Long size = redisTemplate.opsForHash().size(cartKey);
        
        if (size != null && size >= MAX_CART_ITEMS) {
            throw new CartException(CommonResultCode.PARAM_INVALID, 
                    "购物车已满，最多可添加" + MAX_CART_ITEMS + "种商品");
        }
    }

    /**
     * 校验并获取SKU信息
     */
    private ProductFeignClient.SkuInfo validateAndFetchSkuInfo(Long skuId) {
        try {
            Result<Map<Long, ProductFeignClient.SkuInfo>> result = 
                    productFeignClient.batchQuerySkus(Collections.singletonList(skuId));
            
            if (result == null || !result.isSuccess() || result.getData() == null) {
                throw new CartException(CommonResultCode.PARAM_INVALID, "商品不存在或已下架");
            }

            ProductFeignClient.SkuInfo skuInfo = result.getData().get(skuId);
            if (skuInfo == null) {
                throw new CartException(CommonResultCode.PARAM_INVALID, "商品不存在或已下架");
            }

            return skuInfo;
        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("【查询商品信息】失败, skuId={}", skuId, e);
            throw new CartException(CommonResultCode.SYSTEM_ERROR, "查询商品信息失败");
        }
    }

    /**
     * 检查库存
     */
    private void checkStock(Long skuId, Integer quantity, ProductFeignClient.SkuInfo skuInfo) {
        if (skuInfo.getStockStatus() == 0) {
            throw new CartException(CommonResultCode.PARAM_INVALID, "商品已售罄");
        }
        
        if (skuInfo.getStockQuantity() < quantity) {
            throw new CartException(CommonResultCode.PARAM_INVALID, 
                    "库存不足，仅剩" + skuInfo.getStockQuantity() + "件");
        }
    }

    /**
     * 检查限购
     */
    private void checkPurchaseLimit(Long userId, Long skuId, Integer quantity, ProductFeignClient.SkuInfo skuInfo) {
        if (skuInfo.getMaxPurchaseLimit() != null && quantity > skuInfo.getMaxPurchaseLimit()) {
            throw new CartException(CommonResultCode.PARAM_INVALID, 
                    "超过限购数量，单次最多购买" + skuInfo.getMaxPurchaseLimit() + "件");
        }
    }

    /**
     * 执行添加到购物车
     */
    private void executeAddToCart(Long userId, Long skuId, ProductFeignClient.SkuInfo skuInfo, 
                                  Integer quantity, String attrs) {
        String cartKey = CART_KEY_PREFIX + userId;
        String field = String.valueOf(skuId);

        // 1. 检查是否已存在
        Boolean exists = redisTemplate.opsForHash().hasKey(cartKey, field);
        
        if (Boolean.TRUE.equals(exists)) {
            // 已存在：累加数量
            Object existingValue = redisTemplate.opsForHash().get(cartKey, field);
            Map<String, Object> existingItem = JSON.parseObject(JSON.toJSONString(existingValue), Map.class);
            Integer currentQty = (Integer) existingItem.get("quantity");
            quantity = currentQty + quantity;
            
            // 再次检查限购
            checkPurchaseLimit(userId, skuId, quantity, skuInfo);
            
            existingItem.put("quantity", quantity);
            redisTemplate.opsForHash().put(cartKey, field, existingItem);
            
            log.info("【添加购物车】商品已存在，更新数量: {} -> {}", currentQty, quantity);
        } else {
            // 不存在：新建
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("skuId", skuId);
            cartItem.put("spuId", skuInfo.getSpuId());
            cartItem.put("productName", skuInfo.getSpuName());
            cartItem.put("productImage", skuInfo.getMainImage());
            cartItem.put("snapshotPrice", skuInfo.getPrice());
            cartItem.put("snapshotAttrs", attrs != null ? attrs : skuInfo.getAttrs());
            cartItem.put("snapshotTime", LocalDateTime.now().toString());
            cartItem.put("quantity", quantity);
            cartItem.put("selected", 1);
            cartItem.put("stockStatus", skuInfo.getStockStatus());
            cartItem.put("maxPurchaseLimit", skuInfo.getMaxPurchaseLimit());

            redisTemplate.opsForHash().put(cartKey, field, cartItem);
            redisTemplate.expire(cartKey, LOGGED_IN_EXPIRE_DAYS, TimeUnit.DAYS);
            
            log.info("【添加购物车】新增商品, userId={}, skuId={}", userId, skuId);
        }

        // 2. 异步同步MySQL
        syncToMySQL(userId, skuId, null);
    }

    /**
     * 批量查询SKU信息
     */
    private Map<Long, ProductFeignClient.SkuInfo> batchQuerySkuInfos(List<Long> skuIds) {
        try {
            Result<Map<Long, ProductFeignClient.SkuInfo>> result = 
                    productFeignClient.batchQuerySkus(skuIds);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("【批量查询SKU】失败, skuIds={}", skuIds, e);
        }
        
        return Collections.emptyMap();
    }

    /**
     * 转换为VO
     */
    private CartItemVO convertToVO(Map<String, Object> rawItem, Map<Long, ProductFeignClient.SkuInfo> skuInfoMap) {
        Long skuId = Long.valueOf(rawItem.get("skuId").toString());
        ProductFeignClient.SkuInfo skuInfo = skuInfoMap.get(skuId);

        CartItemVO.CartItemVOBuilder builder = CartItemVO.builder()
                .skuId(skuId)
                .spuId(Long.valueOf(rawItem.get("spuId").toString()))
                .productName((String) rawItem.get("productName"))
                .productImage((String) rawItem.get("productImage"))
                .snapshotPrice(new BigDecimal(rawItem.get("snapshotPrice").toString()))
                .attrs((String) rawItem.get("snapshotAttrs"))
                .quantity((Integer) rawItem.get("quantity"))
                .selected((Integer) rawItem.get("selected"));

        if (skuInfo != null) {
            // 商品有效
            builder.valid(true)
                    .currentPrice(skuInfo.getPrice())
                    .promotionPrice(skuInfo.getPromotionPrice())
                    .stockStatus(skuInfo.getStockStatus())
                    .stockQuantity(skuInfo.getStockQuantity())
                    .maxPurchaseLimit(skuInfo.getMaxPurchaseLimit())
                    .promotionTags(skuInfo.getPromotionTags())
                    .promotionId(skuInfo.getPromotionId())
                    .productSubtitle(skuInfo.getSubtitle());

            // 计算小计
            BigDecimal actualPrice = skuInfo.getPromotionPrice() != null ? 
                    skuInfo.getPromotionPrice() : skuInfo.getPrice();
            builder.subtotal(actualPrice.multiply(new BigDecimal(builder.build().getQuantity())));

            // 价格变动标识
            BigDecimal snapshotPrice = builder.build().getSnapshotPrice();
            if (actualPrice.compareTo(snapshotPrice) > 0) {
                builder.priceChangeFlag(1); // 涨价
            } else if (actualPrice.compareTo(snapshotPrice) < 0) {
                builder.priceChangeFlag(2); // 降价
            } else {
                builder.priceChangeFlag(0); // 无变化
            }

            // 检查是否超过限购
            if (skuInfo.getMaxPurchaseLimit() != null && builder.build().getQuantity() > skuInfo.getMaxPurchaseLimit()) {
                builder.exceedLimit(true);
            } else {
                builder.exceedLimit(false);
            }
        } else {
            // 商品失效
            builder.valid(false)
                    .invalidReason("商品已下架或删除")
                    .stockStatus(0);
        }

        return builder.build();
    }

    /**
     * 计算购物车汇总
     */
    private CartSummaryVO calculateSummary(Long userId, List<CartItemVO> validItems, 
                                           List<CartItemVO> invalidItems, List<String> tips) {
        // 筛选已选中的商品
        List<CartItemVO> selectedItems = validItems.stream()
                .filter(item -> item.getSelected() == 1)
                .collect(Collectors.toList());

        // 计算统计信息
        int totalQuantity = validItems.stream().mapToInt(CartItemVO::getQuantity).sum();
        int selectedCount = selectedItems.stream().mapToInt(CartItemVO::getQuantity).sum();
        
        BigDecimal originalTotal = validItems.stream()
                .map(item -> item.getSnapshotPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal payableAmount = selectedItems.stream()
                .map(CartItemVO::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = originalTotal.subtract(payableAmount);

        boolean allSelected = validItems.size() > 0 && validItems.size() == selectedItems.size();
        boolean canCheckout = !validItems.isEmpty() && invalidItems.isEmpty() && selectedCount > 0;

        return CartSummaryVO.builder()
                .userId(userId)
                .validItems(validItems)
                .invalidItems(invalidItems)
                .totalQuantity(totalQuantity)
                .totalItemCount(validItems.size())
                .selectedCount(selectedItems.size())
                .originalTotalAmount(originalTotal)
                .payableAmount(payableAmount)
                .totalDiscountAmount(discountAmount.max(BigDecimal.ZERO))
                .allSelected(allSelected)
                .tips(tips)
                .hasInvalidItems(!invalidItems.isEmpty())
                .canCheckout(canCheckout)
                .checkoutBlockedReason(canCheckout ? null : getBlockedReason(validItems, invalidItems, selectedCount))
                .build();
    }

    /**
     * 获取不可结算原因
     */
    private String getBlockedReason(List<CartItemVO> validItems, List<CartItemVO> invalidItems, int selectedCount) {
        if (validItems.isEmpty() && invalidItems.isEmpty()) {
            return "购物车为空";
        }
        if (!invalidItems.isEmpty()) {
            return "存在失效商品，请清理后再结算";
        }
        if (selectedCount == 0) {
            return "请选择要结算的商品";
        }
        return null;
    }

    /**
     * 构建空购物车汇总
     */
    private CartSummaryVO buildEmptyCartSummary(Long userId) {
        return CartSummaryVO.builder()
                .userId(userId)
                .validItems(Collections.emptyList())
                .invalidItems(Collections.emptyList())
                .totalQuantity(0)
                .totalItemCount(0)
                .selectedCount(0)
                .originalTotalAmount(BigDecimal.ZERO)
                .payableAmount(BigDecimal.ZERO)
                .totalDiscountAmount(BigDecimal.ZERO)
                .allSelected(false)
                .tips(Collections.singletonList("购物车还是空的，快去逛逛吧~"))
                .hasInvalidItems(false)
                .canCheckout(false)
                .checkoutBlockedReason("购物车为空")
                .build();
    }

    /**
     * 同步到MySQL（异步持久化 - RocketMQ）
     * <p>
     * 业界标准：使用RocketMQ异步同步，解耦Redis和MySQL写入
     * - 提升接口响应速度
     * - 支持重试机制保证最终一致性
     * - 失败自动重试，最多3次
     * </p>
     */
    private void syncToMySQL(Long userId, Long skuId, Map<String, Object> cartItem) {
        try {
            if (cartItem == null) {
                String cartKey = CART_KEY_PREFIX + userId;
                Object value = redisTemplate.opsForHash().get(cartKey, String.valueOf(skuId));
                if (value == null) {
                    return;
                }
                cartItem = JSON.parseObject(JSON.toJSONString(value), Map.class);
            }

            // 构建消息
            CartSyncMessage message = CartSyncMessage.builder()
                    .userId(userId)
                    .skuId(skuId)
                    .spuId(Long.valueOf(cartItem.get("spuId").toString()))
                    .productName((String) cartItem.get("productName"))
                    .productImage((String) cartItem.get("productImage"))
                    .snapshotPrice(new BigDecimal(cartItem.get("snapshotPrice").toString()))
                    .snapshotAttrs((String) cartItem.get("snapshotAttrs"))
                    .quantity((Integer) cartItem.get("quantity"))
                    .selected((Integer) cartItem.get("selected"))
                    .operationType("ADD")
                    .timestamp(LocalDateTime.now())
                    .build();

            // 异步发送消息
            cartSyncProducer.sendAsyncSyncMessage(message);
        } catch (Exception e) {
            log.error("【发送同步消息】失败, userId={}, skuId={}", userId, skuId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 同步合并后的购物车到MySQL（RocketMQ）
     */
    private void syncMergedCartToMySQL(Long userId, String cartKey) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);
            if (entries == null || entries.isEmpty()) {
                return;
            }

            // 批量发送消息
            for (Object value : entries.values()) {
                Map<String, Object> cartItem = JSON.parseObject(JSON.toJSONString(value), Map.class);
                Long skuId = Long.valueOf(cartItem.get("skuId").toString());
                
                CartSyncMessage message = CartSyncMessage.builder()
                        .userId(userId)
                        .skuId(skuId)
                        .spuId(Long.valueOf(cartItem.get("spuId").toString()))
                        .productName((String) cartItem.get("productName"))
                        .productImage((String) cartItem.get("productImage"))
                        .snapshotPrice(new BigDecimal(cartItem.get("snapshotPrice").toString()))
                        .snapshotAttrs((String) cartItem.get("snapshotAttrs"))
                        .quantity((Integer) cartItem.get("quantity"))
                        .selected((Integer) cartItem.get("selected"))
                        .operationType("MERGE")
                        .timestamp(LocalDateTime.now())
                        .build();

                cartSyncProducer.sendAsyncSyncMessage(message);
            }

            log.info("【发送合并购物车同步消息】成功, userId={}, itemCount={}", userId, entries.size());
        } catch (Exception e) {
            log.error("【发送合并购物车同步消息】失败, userId={}", userId, e);
            // 不抛出异常，避免影响主流程
        }
    }
}
