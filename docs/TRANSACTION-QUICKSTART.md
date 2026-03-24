# NexusMall 分布式事务快速开始指南

## ✅ 已完成的工作

### 1. nexusmall-auth 模块
✅ 添加了 5 个本地事务方法:
- `register()` - 用户注册 (包含分配角色)
- `updateUser()` - 更新用户信息
- `deleteUser()` - 删除用户 (包含删除角色关联)
- `assignRolesToUser()` - 为用户分配角色
- `assignPermissionsToRole()` - 为角色分配权限

✅ 新增实体类:
- `UserRole.java` - 用户角色关联实体
- `RolePermission.java` - 角色权限关联实体

✅ 新增 Mapper 方法:
- `UserMapper.insertUserRole()` - 插入用户角色关联
- `UserMapper.deleteUserRoles()` - 删除用户角色关联
- `RoleMapper.insertRolePermission()` - 插入角色权限关联
- `RoleMapper.deleteRolePermissions()` - 删除角色权限关联

### 2. nexusmall-order 模块
✅ 已有完整的 Seata 配置
✅ `createOrder()` 方法已添加 `@GlobalTransactional` 注解 (全局事务)
✅ 其他 7 个方法已添加 `@Transactional` 注解 (本地事务)

### 3. nexusmall-product 模块
✅ 已有完整的 Seata 配置
✅ 所有 8 个写操作方法已添加 `@Transactional` 注解

---

## 🚀 启动步骤

### 前置要求

1. **MySQL 8.0+** - 确保已启动
2. **Redis** - 确保已启动
3. **Nacos** - 确保已启动 (端口 8848)
4. **Seata Server** - 需要启动 (端口 8091)

### 1. 启动 Seata Server

```bash
# Windows
cd path\to\seata-1.5.2
bin\seata-server.bat

# Linux/Mac
cd path/to/seata-1.5.2
bin/seata-server.sh
```

**验证**: 访问 http://localhost:8091/ 应该能看到 Seata 控制台

### 2. 检查数据库表

确保以下数据库都已创建 `undo_log` 表:
- `nexusmall_auth`
- `nexusmall_order`
- `nexusmall_product`

SQL 脚本位置:
- `auth-table.sql`
- `order-table.sql`
- `product-table.sql`

### 3. 启动微服务

按以下顺序启动服务:

```bash
# 1. 公共模块 (可选，已经编译好)
cd nexusmall-common
mvn spring-boot:run

# 2. 商品服务
cd nexusmall-product
mvn spring-boot:run

# 3. 订单服务
cd nexusmall-order
mvn spring-boot:run

# 4. 认证服务
cd nexusmall-auth
mvn spring-boot:run

# 5. 网关服务
cd nexusmall-gateway
mvn spring-boot:run
```

**IDEA 方式**: 直接运行各个模块的 Application 主类

### 4. 验证服务启动成功

检查日志中是否有以下信息:
```
Started NexusmallXxxApplication in X.XXX seconds
```

---

## 🧪 测试事务

### 测试 1: Auth 模块本地事务

#### 注册用户 (带角色)
```bash
curl -X POST "http://localhost:88/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000",
    "roleIds": [1, 2]
  }'
```

**预期结果**:
- 成功：返回 `true`,用户和角色关联都创建成功
- 失败：如果角色 ID 不存在，整个操作回滚

#### 为用户分配角色
```bash
curl -X POST "http://localhost:88/auth/assignRolesToUser?userId=1&roleIds=1,2,3"
```

**预期结果**:
- 原有的角色关联被删除
- 新的角色关联被创建
- 任何一步失败都会回滚

### 测试 2: Order 模块全局事务

#### 创建订单 (跨服务调用)
```bash
curl -X POST "http://localhost:88/order/create" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "productId": 1,
    "count": 1,
    "skuName": "测试商品",
    "price": 99.99,
    "totalAmount": 99.99,
    "payAmount": 89.99,
    "freightAmount": 10.00,
    "promotionAmount": 10.00,
    "paymentType": 1,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区 xxx",
    "remark": "测试订单"
  }'
```

**预期结果**:
- 成功：订单创建成功，库存扣减成功
- 失败：如果库存不足，订单和库存都回滚

**查看事务日志**:
```
# 应该能看到类似以下的日志
Begin a new global transaction, xid: 192.168.1.100:8091:1234567890
Insert to undo_log table: xid=192.168.1.100:8091:1234567890, branchId=1234567891
Commit the global transaction: xid=192.168.1.100:8091:1234567890
```

### 测试 3: Product 模块本地事务

#### 扣减库存
```bash
curl -X POST "http://localhost:10000/product/decreaseStock?productId=1&count=1"
```

**预期结果**:
- 成功：库存减少
- 失败：库存不足时抛出异常并回滚

---

## 📊 监控事务

### 1. 查看 Seata 控制台

访问：http://localhost:8091/

可以看到:
- 全局事务列表
- 分支事务详情
- 事务提交/回滚记录

### 2. 查看 undo_log 表

```sql
-- 查看最近的事务记录
SELECT * FROM undo_log 
ORDER BY log_created DESC 
LIMIT 10;

-- 查看特定 XID 的事务
SELECT * FROM undo_log 
WHERE xid = '你的 XID';
```

### 3. 查看应用日志

**Order 模块日志**:
```
logs/nexusmall-order/2026-03-24.0.log
```

**Product 模块日志**:
```
logs/nexusmall-product/2026-03-24.0.log
```

**Auth 模块日志**:
```
logs/nexusmall-auth/2026-03-24.0.log
```

---

## 🔍 故障排查

### 问题 1: Seata Server 连接失败

**错误信息**:
```
connect to 127.0.0.1:8091 failed
```

**解决方案**:
1. 检查 Seata Server 是否启动
2. 检查端口 8091 是否被占用
3. 检查 `application.yaml` 中的 Seata 配置

### 问题 2: undo_log 表不存在

**错误信息**:
```
Table 'xxx.undo_log' doesn't exist
```

**解决方案**:
在对应的数据库中执行建表 SQL

### 问题 3: 事务不回滚

**检查点**:
1. 方法是否加了 `@Transactional` 或 `@GlobalTransactional`
2. 方法是否是 `public` 的
3. 异常是否被 catch 后没有重新抛出
4. 是否指定了 `rollbackFor = Exception.class`

### 问题 4: Feign 调用事务不生效

**检查点**:
1. 是否添加了 Seata 依赖
2. Feign 接口是否配置正确
3. 查看日志中 XID 是否传播

---

## 📝 开发建议

### 1. 新增事务方法

```java
// 本地事务
@Override
@Transactional(rollbackFor = Exception.class)
public void yourLocalMethod() {
    // 数据库操作
}

// 全局事务 (跨服务)
@Override
@GlobalTransactional(name = "your-tx-name", rollbackFor = Exception.class)
@Transactional(rollbackFor = Exception.class)
public void yourGlobalMethod() {
    // 本地操作
    // 远程调用
}
```

### 2. 日志记录

在事务方法开始和结束时记录日志:
```java
log.info("开始执行 xxx，参数：{}", param);
// 业务逻辑
log.info("xxx 执行成功");
```

### 3. 异常处理

不要吞掉异常，让事务管理器处理:
```java
// ❌ 错误示例
try {
    // 操作
} catch (Exception e) {
    log.error("失败", e);
    // 没有重新抛出
}

// ✅ 正确示例
try {
    // 操作
} catch (Exception e) {
    log.error("失败", e);
    throw e; // 重新抛出
}
```

---

## 📚 参考文档

- [TRANSACTION-GUIDE.md](./TRANSACTION-GUIDE.md) - 完整事务配置指南
- [Seata 官方文档](https://seata.io/zh-cn/)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba)

---

## ✨ 总结

本次为 NexusMall 项目添加了完整的分布式事务支持:

1. **Auth 模块**: 5 个本地事务方法，用于用户管理和角色权限管理
2. **Order 模块**: 1 个全局事务 + 7 个本地事务，用于订单管理
3. **Product 模块**: 8 个本地事务方法，用于商品和库存管理

所有事务都按照 Spring 和 Seata 的最佳实践配置，确保数据一致性。

**下一步建议**:
- 编写单元测试验证事务回滚
- 配置 Seata 监控告警
- 优化事务超时时间
- 添加事务成功率监控

---

**更新时间**: 2026-03-24  
**维护人员**: AI Assistant
