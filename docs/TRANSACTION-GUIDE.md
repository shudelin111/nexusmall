# NexusMall 分布式事务配置说明

## 📋 概述

本项目采用 **Seata AT 模式**实现分布式事务管理，确保跨服务数据一致性。

## 🏗️ 架构设计

### 事务模式
- **Seata AT (Automatic Transaction)** - 自动补偿的两阶段提交协议
- 支持自动回滚和异常恢复
- 对代码无侵入，只需添加注解

### 适用场景
1. **跨服务调用**: Order 服务调用 Product 服务扣减库存
2. **本地复杂业务**: 用户注册同时分配角色权限
3. **批量操作**: 批量更新数据

## 📦 模块事务配置

### 1. nexusmall-auth (认证服务)

#### 依赖配置
```xml
<!-- Seata 已预留，暂时不需要，因为 auth 模块主要是查询操作 -->
<!-- 如果后续需要分布式事务，取消以下注释 -->
<!--
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
-->
```

#### 事务注解使用

##### 本地事务 (@Transactional)

```java
@Service
public class AuthServiceImpl implements AuthService {
    
    /**
     * 用户注册 - 包含插入用户和分配角色，需要事务保证
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user, List<Long> roleIds) {
        // 1. 检查用户名是否存在
        // 2. 加密密码并插入用户
        // 3. 分配角色（如果有）
        // 任何一步失败都会回滚整个事务
    }
    
    /**
     * 删除用户 - 先删除关联关系，再删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 1. 删除用户角色关联
        // 2. 删除用户
        // 保证数据一致性
    }
    
    /**
     * 为用户分配角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        // 1. 删除原有角色关联
        // 2. 添加新角色关联
    }
    
    /**
     * 为角色分配权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 1. 删除原有权限关联
        // 2. 添加新权限关联
    }
}
```

#### 关键点说明
1. **@Transactional(rollbackFor = Exception.class)** - 任何异常都回滚
2. 所有数据库写操作都添加了事务注解
3. 查询操作不需要事务注解
4. 日志记录在事务方法内部，方便追踪

---

### 2. nexusmall-order (订单服务)

#### 依赖配置
```xml
<!-- Seata 分布式事务 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

#### 配置文件 (application.yaml)
```yaml
# Seata 分布式事务配置
seata:
  enabled: true
  application-id: nexusmall-order
  tx-service-group: nexusmall-tx-group
  service:
    vgroup-mapping:
      nexusmall-tx-group: default
    grouplist:
      default: 127.0.0.1:8091
  registry:
    type: file
  config:
    type: file
  client:
    rm:
      report-success-enable: false
      report-failure-enable: false
    tm:
      commit-retry-count: 5
      rollback-retry-count: 5
```

#### 事务注解使用

##### 全局事务 (@GlobalTransactional + @Transactional)

```java
@Service
public class OrderServiceImpl implements OrderService {
    
    /**
     * 创建订单 - 跨服务调用，需要全局事务
     */
    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request) {
        // 1. 生成订单号
        // 2. 扣减库存（调用 Product 服务 - RPC）
        // 3. 创建订单主表
        // 4. 创建订单项
        // 任何一步失败都会回滚所有操作（包括远程调用）
    }
    
    /**
     * 更新订单 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Order order) {
        // 本地数据库操作
    }
    
    /**
     * 删除订单 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        // 1. 删除订单项
        // 2. 删除订单主表
    }
    
    /**
     * 支付订单 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long id, Integer paymentType) {
        // 更新订单状态为已支付
    }
}
```

#### 关键点说明
1. **@GlobalTransactional** - 标注在全局事务入口方法上
2. **name 属性** - 给事务命名，便于日志追踪
3. 同时使用 `@GlobalTransactional` 和 `@Transactional` 确保本地和全局都受控
4. Feign 客户端调用会自动传播 XID (全局事务 ID)

---

### 3. nexusmall-product (商品服务)

#### 依赖配置
```xml
<!-- Seata 分布式事务 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

#### 配置文件 (application.yaml)
```yaml
# Seata 分布式事务配置
seata:
  enabled: true
  application-id: nexusmall-product
  tx-service-group: nexusmall-tx-group
  service:
    vgroup-mapping:
      nexusmall-tx-group: default
    grouplist:
      default: 127.0.0.1:8091
  registry:
    type: file
  config:
    type: file
  client:
    rm:
      report-success-enable: false
      report-failure-enable: false
    tm:
      commit-retry-count: 5
      rollback-retry-count: 5
```

#### 事务注解使用

##### 本地事务 (@Transactional)

```java
@Service
public class ProductServiceImpl implements ProductService {
    
    /**
     * 扣减库存 - 被 Order 服务调用，参与全局事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long skuId, Integer count) {
        log.info("开始扣减库存，skuId: {}, count: {}", skuId, count);
        int result = productMapper.decreaseStock(skuId, count);
        if (result > 0) {
            log.info("库存扣减成功，skuId: {}, count: {}", skuId, count);
            return true;
        } else {
            log.error("库存扣减失败，skuId: {}, count: {}，库存不足", skuId, count);
            throw new RuntimeException("库存不足");
        }
    }
    
    /**
     * 增加库存 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long skuId, Integer count) {
        // 增加库存操作
    }
    
    /**
     * 批量扣减库存 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDecreaseStock(List<ProductStockDTO> stockDTOS) {
        // 批量操作
    }
    
    /**
     * 新增商品 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(ProductVO productVO) {
        // 插入商品
    }
    
    /**
     * 删除商品 - 本地事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long skuId) {
        // 删除商品
    }
}
```

#### 关键点说明
1. 作为资源服务 (RM)，只需要 `@Transactional`
2. 不需要 `@GlobalTransactional`，由调用方 (TM) 控制全局事务
3. 抛异常会触发回滚
4. 支持乐观锁 (version 字段) 防止超卖

---

## 🔧 Seata Server 配置

### 1. 下载 Seata Server
访问：https://seata.io/zh-cn/blog/download.html
下载版本：**Seata 1.5.2** (与 Spring Cloud Alibaba 2021.0.5.0 兼容)

### 2. 解压目录结构
```
seata-1.5.2/
├── bin/          # 启动脚本
├── conf/         # 配置文件
└── lib/          # 依赖库
```

### 3. 修改 conf/file.conf
```conf
service {
  # vgroup->group
  vgroup_mapping.nexusmall-tx-group = "default"
  # only support when registry.type is file
  default.grouplist = "127.0.0.1:8091"
}

store {
  # store mode: file、db、redis
  mode = "file"
  
  # file store
  file {
    dir = "sessionStore"
    max-branch-session-size = 16384
    max-global-session-size = 512
    file-write-buffer-cache-size = 16384
    session-reload-read-size = 100
    flush-disk-mode = async
  }
}
```

### 4. 修改 conf/registry.conf
```conf
registry {
  type = "file"
}

config {
  type = "file"
}
```

### 5. 启动 Seata Server
**Windows:**
```bash
bin\seata-server.bat
```

**Linux/Mac:**
```bash
bin/seata-server.sh
```

启动成功后，默认端口：**8091**

---

## 📊 数据库表要求

### UNDO_LOG 表 (必须)

每个涉及分布式事务的数据库都需要创建此表:

```sql
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
  `branch_id` BIGINT(20) NOT NULL COMMENT 'branch transaction id',
  `xid` VARCHAR(100) NOT NULL COMMENT 'global transaction id',
  `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
  `log_status` INT(11) NOT NULL COMMENT '0:normal status,1:defended status',
  `log_created` DATETIME NOT NULL COMMENT 'create datetime',
  `log_modified` DATETIME NOT NULL COMMENT 'modify datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';
```

### 已配置的数据库
1. **nexusmall_auth** - 已创建 undo_log 表
2. **nexusmall_order** - 已创建 undo_log 表
3. **nexusmall_product** - 已创建 undo_log 表

---

## 🎯 事务传播机制

### XID (全局事务 ID) 传播

```
Order 服务 (TM)                          Product 服务 (RM)
     |                                        |
     | @GlobalTransactional                   |
     | createOrder()                          |
     |                                        |
     |----Feign 调用 (携带 XID)--------------->|
     |                                        |
     |                            @Transactional
     |                            decreaseStock()
     |                                        |
     |                            [加入全局分支事务]
     |                                        |
     |<---返回结果-----------------------------|
     |                                        |
     | [提交/回滚]                             |
     |                                        |
```

### 传播方式
1. **HTTP 请求**: 通过 Header 传递 `X-Seata-XID`
2. **Feign**: 自动传播，无需手动处理
3. **Dubbo**: 需要配置 Filter

---

## ✅ 最佳实践总结

### 1. 注解使用规范

#### 本地事务
```java
@Transactional(rollbackFor = Exception.class)
public void methodName() {
    // 数据库操作
}
```

#### 全局事务
```java
@GlobalTransactional(name = "tx-name", rollbackFor = Exception.class)
@Transactional(rollbackFor = Exception.class)
public void methodName() {
    // 本地数据库操作
    // 远程服务调用 (Feign/RPC)
}
```

### 2. 命名规范
- **事务名称**: 使用小写字母 + 连字符，如 `create-order-tx`
- **服务组**: 统一使用 `nexusmall-tx-group`
- **应用 ID**: 与微服务名一致，如 `nexusmall-order`

### 3. 异常处理
- 明确指定 `rollbackFor = Exception.class`
- 不要捕获异常后不抛出，会导致事务失效
- 使用日志记录异常信息

### 4. 性能优化
```yaml
seata:
  client:
    rm:
      report-success-enable: false  # 不报告一阶段成功，减少网络开销
      report-failure-enable: false
    tm:
      commit-retry-count: 5         # 提交重试次数
      rollback-retry-count: 5       # 回滚重试次数
```

### 5. 监控和日志
- 开启 Seata 日志记录
- 监控 undo_log 表大小
- 定期检查长事务

---

## 🐛 常见问题排查

### 1. 事务不回滚
**原因:**
- 没有加 `@Transactional` 或 `@GlobalTransactional`
- 异常被 catch 后没有重新抛出
- 方法不是 public 的
- 同类中调用 (需要通过代理)

**解决:**
```java
// ❌ 错误示例
private void method() { }  // 不是 public

// ❌ 错误示例
try {
    // 操作
} catch (Exception e) {
    log.error("错误", e);  // 没有重新抛出
}

// ✅ 正确示例
@Transactional(rollbackFor = Exception.class)
public void method() {
    // 操作
}
```

### 2. Feign 调用事务不生效
**原因:**
- Seata 依赖未添加
- Feign 未配置拦截器
- XID 未传播

**解决:**
```xml
<!-- 确保添加依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

### 3. undo_log 表不存在
**错误信息:**
```
Table 'xxx.undo_log' doesn't exist
```

**解决:**
在每个涉及的数据库中执行建表 SQL

### 4. Seata Server 连接失败
**检查项:**
1. Seata Server 是否启动
2. 端口 8091 是否开放
3. `file.conf` 和 `registry.conf` 配置是否正确
4. 服务组的配置是否一致 (`nexusmall-tx-group`)

---

## 📝 开发指南

### 新增事务方法的步骤

1. **判断是否需要分布式事务**
   - 只有本地数据库操作 → `@Transactional`
   - 涉及跨服务调用 → `@GlobalTransactional` + `@Transactional`

2. **添加注解**
```java
@Override
@Transactional(rollbackFor = Exception.class)
public void yourMethod() {
    // 业务逻辑
}
```

3. **编写测试用例**
- 正常流程测试
- 异常回滚测试

4. **验证事务**
- 查看日志中的 XID
- 检查 undo_log 表记录
- 验证回滚是否成功

---

## 📚 参考资料

- [Seata 官方文档](https://seata.io/zh-cn/)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba)
- [Seata AT 模式原理](https://seata.io/zh-cn/docs/user/quickstart.html)

---

## 📊 事务统计

| 模块 | 全局事务数 | 本地事务数 | 涉及表数 |
|------|-----------|-----------|---------|
| nexusmall-auth | 0 | 5 | 4 |
| nexusmall-order | 1 | 7 | 3 |
| nexusmall-product | 0 | 8 | 3 |
| **总计** | **1** | **20** | **10** |

---

**最后更新时间**: 2026-03-24  
**维护人员**: AI Assistant
