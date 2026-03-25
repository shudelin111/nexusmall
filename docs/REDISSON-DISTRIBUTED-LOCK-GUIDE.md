# Redisson 分布式锁使用指南

## 一、概述

项目已集成 Redisson 分布式锁，提供了两种使用方式：
1. **工具类方式**：通过 `DistributedLockUtil` 手动加锁
2. **注解方式**：通过 `@DistributedLock` 注解自动加锁（推荐）

## 二、依赖说明

### 核心组件
- **RedissonClient**：Redisson 客户端，由 `nexusmall-common` 提供
- **DistributedLockUtil**：分布式锁工具类
- **DistributedLockAspect**：AOP 切面处理器
- **@DistributedLock**：分布式锁注解

### Maven 依赖
```xml
<!-- nexusmall-common 中已包含 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.21.3</version>
</dependency>
```

## 三、使用方式

### 方式一：注解方式（推荐）

在需要加分布式锁的方法上添加 `@DistributedLock` 注解：

```java
import com.nexusmall.common.annotation.DistributedLock;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    /**
     * 创建订单（带分布式锁）
     * @param userId 用户 ID
     * @param productId 商品 ID
     */
    @DistributedLock(key = "#userId + ':order:' + #productId", waitTime = 5, leaseTime = 30)
    public void createOrder(Long userId, Long productId) {
        // 业务逻辑：创建订单
        // 方法执行前会自动获取锁，执行后自动释放
    }

    /**
     * 扣减库存（带分布式锁）
     * @param productId 商品 ID
     * @param quantity 数量
     */
    @DistributedLock(key = "'stock:' + #productId")
    public void deductStock(Long productId, Integer quantity) {
        // 业务逻辑：扣减库存
    }
}
```

#### 注解参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `key` | String | 方法签名 | 锁的键名，支持 SpEL 表达式 |
| `waitTime` | long | 5 | 等待时间（秒），获取锁的最大等待时间 |
| `leaseTime` | long | 30 | 锁持有时间（秒），-1 表示不自动释放 |

#### SpEL 表达式示例

```java
// 使用方法参数
@DistributedLock(key = "#userId")
public void userOperation(Long userId) { }

// 组合键
@DistributedLock(key = "'seckill:' + #productId + ':' + #userId")
public void seckill(Long productId, Long userId) { }

// 固定前缀 + 参数
@DistributedLock(key = "'lock:order:' + #orderId")
public void processOrder(String orderId) { }
```

### 方式二：工具类方式

通过注入 `DistributedLockUtil` 手动控制锁：

```java
import com.nexusmall.common.util.DistributedLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    /**
     * 扣减库存（手动加锁）
     */
    public void deductStock(Long productId, Integer quantity) {
        String lockKey = "stock:" + productId;
        
        // 方式 1：有返回值的执行
        Boolean result = distributedLockUtil.executeWithLock(lockKey, () -> {
            // 加锁后的业务逻辑
            return doDeductStock(productId, quantity);
        });
        
        // 方式 2：无返回值的执行
        distributedLockUtil.executeWithLock(lockKey, () -> {
            // 加锁后的业务逻辑
            doDeductStock(productId, quantity);
        });
        
        // 方式 3：自定义参数
        distributedLockUtil.executeWithLock(
            lockKey, 
            10,  // 等待 10 秒
            60,  // 锁定 60 秒
            TimeUnit.SECONDS,
            () -> doDeductStock(productId, quantity)
        );
    }

    private Boolean doDeductStock(Long productId, Integer quantity) {
        // 实际扣减库存的逻辑
        return true;
    }
}
```

## 四、典型应用场景

### 1. 秒杀场景

```java
@Service
public class SeckillService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 秒杀下单
     */
    @DistributedLock(key = "'seckill:' + #seckillId", waitTime = 3, leaseTime = 10)
    public SeckillResult seckill(Long seckillId, Long userId) {
        // 1. 检查库存
        Product product = productMapper.getById(seckillId);
        if (product.getStock() <= 0) {
            return SeckillResult.OUT_OF_STOCK;
        }

        // 2. 扣减库存
        productMapper.deductStock(seckillId);

        // 3. 创建订单
        createOrder(seckillId, userId);

        return SeckillResult.SUCCESS;
    }
}
```

### 2. 防止重复提交

```java
@Service
public class OrderSubmitService {

    /**
     * 提交订单（防止重复提交）
     */
    @DistributedLock(key = "'submit:' + #userId + ':' + #orderId", waitTime = 2, leaseTime = 5)
    public void submitOrder(Long userId, String orderId) {
        // 检查是否已提交
        if (isAlreadySubmitted(orderId)) {
            throw new RuntimeException("订单已提交");
        }

        // 执行业务逻辑
        processOrder(userId, orderId);
    }
}
```

### 3. 定时任务防并发

```java
@Service
public class ScheduledTaskService {

    /**
     * 每日结算任务（防止多实例并发执行）
     */
    @DistributedLock(key = "'task:daily:settlement'", waitTime = 0, leaseTime = 3600)
    public void dailySettlement() {
        // 每日结算逻辑
        // waitTime=0 表示立即返回，如果锁已被其他实例持有则不执行
    }
}
```

### 4. 库存扣减

```java
@Service
public class InventoryService {

    /**
     * 扣减库存
     */
    @DistributedLock(key = "'inventory:' + #skuId", waitTime = 5, leaseTime = 30)
    public boolean deductInventory(Long skuId, Integer quantity) {
        // 查询当前库存
        Inventory inventory = inventoryMapper.getBySkuId(skuId);
        
        if (inventory.getQuantity() < quantity) {
            return false; // 库存不足
        }

        // 扣减库存
        inventoryMapper.deductQuantity(skuId, quantity);
        
        return true;
    }
}
```

## 五、注意事项

### 1. 锁的粒度

- **推荐**：使用细粒度锁（如针对具体资源 ID）
- **避免**：使用粗粒度锁（如全局锁）

```java
// ✅ 好的做法：每个商品一把锁
@DistributedLock(key = "'stock:' + #productId")
public void deductStock(Long productId) { }

// ❌ 不好的做法：全局锁，影响并发性能
@DistributedLock(key = "'global:stock'")
public void deductStock(Long productId) { }
```

### 2. 锁的超时时间

- **waitTime**：获取锁的等待时间，建议 3-5 秒
- **leaseTime**：锁持有时间，建议 10-30 秒
- 避免设置过长，防止死锁

### 3. 异常处理

- 获取锁失败会抛出 `RuntimeException`
- 建议在调用方捕获异常并处理

```java
try {
    service.method();
} catch (RuntimeException e) {
    if (e.getMessage().contains("获取分布式锁失败")) {
        // 处理锁竞争失败的情况
        log.warn("操作繁忙，请稍后重试");
    } else {
        throw e;
    }
}
```

### 4. 事务与锁的顺序

```java
// ✅ 正确：先加锁，后开启事务
@DistributedLock(key = "#id")
@Transactional
public void correctMethod(Long id) { }

// ❌ 错误：事务提交后锁才释放
@Transactional
@DistributedLock(key = "#id")
public void wrongMethod(Long id) { }
```

## 六、配置说明

### Redis 连接配置

在 `application.yaml` 中配置 Redis 连接信息：

```yaml
spring:
  redis:
    host: 10.10.1.1
    port: 6379
    password: 123456
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 0
```

### Redisson 配置

Redisson 配置类位于 `com.nexusmall.common.config.RedissonConfig`，可根据需要调整：

```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
            .setAddress(address)
            .setDatabase(redisDatabase)
            .setPassword(redisPassword.isEmpty() ? null : redisPassword)
            .setConnectionMinimumIdleSize(8)
            .setConnectionPoolSize(32)
            .setIdleConnectionTimeout(10000)
            .setConnectTimeout(10000)
            .setTimeout(3000);
    
    return Redisson.create(config);
}
```

## 七、监控与调试

### 1. 查看锁状态

```bash
# 查看 Redis 中的锁
KEYS lock:*

# 查看锁的详细信息
HGETALL redisson_lock:{lockKey}
```

### 2. 日志记录

在切面中添加日志输出（可选）：

```java
@Around("@annotation(com.nexusmall.common.annotation.DistributedLock)")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    String lockKey = parseLockKey(...);
    log.info("获取分布式锁：{}", lockKey);
    
    try {
        Object result = joinPoint.proceed();
        log.info("释放分布式锁：{}", lockKey);
        return result;
    } catch (Exception e) {
        log.error("分布式锁执行异常：{}", lockKey, e);
        throw e;
    }
}
```

## 八、常见问题

### Q1: 锁无法释放怎么办？

Redisson 提供了看门狗机制（watchdog），如果未指定 `leaseTime` 或设置为 -1，会自动续期锁。

### Q2: 如何避免死锁？

- 设置合理的 `leaseTime`
- 确保 finally 块中释放锁
- 使用 Redisson 的自动释放机制

### Q3: 分布式锁的性能如何？

- 单次加锁耗时约 1-5ms（取决于网络延迟）
- 建议在高并发场景下使用本地缓存 + 分布式锁的组合方案

## 九、参考资料

- [Redisson 官方文档](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8)
- [Redlock 算法](http://redis.io/topics/distlock)
