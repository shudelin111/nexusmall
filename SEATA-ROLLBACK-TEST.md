# Seata 分布式事务回滚测试指南

## 📋 修复内容总结

### 1. ✅ 启用了 Seata RM 报告
- **nexusmall-order/application.yaml**: `report-success-enable: true`, `report-failure-enable: true`
- **nexusmall-product/application.yaml**: `report-success-enable: true`, `report-failure-enable: true`

### 2. ✅ 添加了 @GlobalTransactional 注解
- **ProductServiceImpl.decreaseStock()**: 添加了 `@GlobalTransactional(name = "decrease-stock-tx", rollbackFor = Exception.class)`

### 3. ✅ 创建了 undo_log 表 SQL 脚本
- 文件位置：`nexusmall-common/src/main/resources/sql/undo_log.sql`
- 包含三个数据库的 undo_log 表创建语句

---

## 🔧 执行步骤

### 第一步：执行 SQL 脚本创建 undo_log 表

在 MySQL 中执行以下命令（或使用 Navicat、DBeaver 等工具）：

```bash
# 方式 1: 使用命令行
mysql -h10.10.1.1 -uroot -p123456 < nexusmall-common/src/main/resources/sql/undo_log.sql

# 方式 2: 分别执行
mysql -h10.10.1.1 -uroot -p123456 -e "USE nexusmall_order; SOURCE D:/IdeaProjects/nexusmall/nexusmall-common/src/main/resources/sql/undo_log.sql"
mysql -h10.10.1.1 -uroot -p123456 -e "USE nexusmall_product; SOURCE D:/IdeaProjects/nexusmall/nexusmall-common/src/main/resources/sql/undo_log.sql"
```

### 第二步：验证 undo_log 表是否存在

```sql
-- 检查 order 数据库
USE nexusmall_order;
SHOW TABLES LIKE 'undo_log';

-- 检查 product 数据库
USE nexusmall_product;
SHOW TABLES LIKE 'undo_log';
```

### 第三步：重启所有服务

停止所有服务后，按顺序启动：

```powershell
# 1. 启动 Auth 服务
cd D:\IdeaProjects\nexusmall\nexusmall-auth
mvn spring-boot:run -DskipTests

# 2. 启动 Product 服务
cd D:\IdeaProjects\nexusmall\nexusmall-product
mvn spring-boot:run -DskipTests

# 3. 启动 Order 服务
cd D:\IdeaProjects\nexusmall\nexusmall-order
mvn spring-boot:run -DskipTests

# 4. 启动 Gateway 服务
cd D:\IdeaProjects\nexusmall\nexusmall-gateway
mvn spring-boot:run -DskipTests
```

### 第四步：正常创建订单测试

```bash
curl -X POST http://localhost:88/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "productId": 1,
    "skuName": "iPhone 15 Pro Max",
    "price": 9999.00,
    "count": 1,
    "totalAmount": 9999.00,
    "payAmount": 9999.00,
    "freightAmount": 0,
    "promotionAmount": 0,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区某某街道",
    "paymentType": 1,
    "remark": "测试订单"
  }'
```

**预期结果**：订单创建成功，库存扣减成功

### 第五步：测试分布式事务回滚

#### 方法 1：启用代码中的测试异常

1. 编辑 `OrderServiceImpl.java`，取消注释：
```java
// 取消这行的注释
throw new RuntimeException("测试回滚 - 订单创建后会回滚");
```

2. 重新编译并重启 Order 服务

3. 再次执行创建订单接口

**预期结果**：
- 订单表没有新数据
- 库存表数量恢复原状
- undo_log 表中有回滚记录

#### 方法 2：制造库存不足的场景

```bash
# 先查询当前库存
curl http://localhost:88/product/1

# 创建订单购买超过库存的数量
curl -X POST http://localhost:88/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "productId": 1,
    "skuName": "iPhone 15 Pro Max",
    "price": 9999.00,
    "count": 9999,
    "totalAmount": 99990000.00,
    "payAmount": 99990000.00,
    "freightAmount": 0,
    "promotionAmount": 0,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区某某街道",
    "paymentType": 1,
    "remark": "测试库存不足"
  }'
```

**预期结果**：
- 返回"库存不足"错误
- 订单表没有数据插入
- undo_log 表应该为空（因为没有开始事务）

---

## 📊 验证回滚是否成功

### 检查订单表
```sql
USE nexusmall_order;
SELECT * FROM `order` ORDER BY id DESC LIMIT 10;
```

### 检查订单项表
```sql
USE nexusmall_order;
SELECT * FROM order_item ORDER BY id DESC LIMIT 10;
```

### 检查库存表
```sql
USE nexusmall_product;
SELECT sku_id, stock FROM product WHERE sku_id = 1;
```

### 检查 undo_log 表
```sql
USE nexusmall_order;
SELECT * FROM undo_log ORDER BY id DESC LIMIT 10;

USE nexusmall_product;
SELECT * FROM undo_log ORDER BY id DESC LIMIT 10;
```

---

## ⚠️ 常见问题排查

### 问题 1：Seata Server 未启动
**现象**：日志显示连接 10.10.1.1:8091 失败

**解决**：
```bash
docker ps | grep seata-server
# 如果未运行，启动它
docker start seata-server
```

### 问题 2：RM 未注册到 Seata
**现象**：日志中没有"register RM success"

**解决**：
- 检查配置文件中 `seata.enabled: true`
- 检查 `seata.service.grouplist.default: 10.10.1.1:8091`
- 重启服务

### 问题 3：事务没有回滚
**可能原因**：
1. undo_log 表不存在 → 执行 SQL 脚本创建
2. RM 报告未启用 → 已修改为 `true`
3. 异常被吞掉没有抛出 → 检查日志

### 问题 4：日志显示"can't find db"
**原因**：undo_log 表不存在或不在正确的数据库中

**解决**：确保在每个参与事务的数据库中都创建了 undo_log 表

---

## 📝 清理测试数据

测试完成后，清理数据：

```sql
-- 清理订单数据
USE nexusmall_order;
DELETE FROM order_item;
DELETE FROM `order`;
DELETE FROM undo_log;

-- 重置库存（如果需要）
USE nexusmall_product;
UPDATE product SET stock = 100 WHERE sku_id = 1;
DELETE FROM undo_log;
```

---

## ✅ 完成标志

当您看到以下日志时，说明分布式事务正常工作：

**Order 服务日志**：
```
Begin global transaction: [xid=xxx]
Committed global transaction: [xid=xxx]
```

**Product 服务日志**：
```
Branch committing: [xid=xxx, branchId=yyy]
Branch commit success: [xid=xxx, branchId=yyy]
```

如果要测试回滚，应该看到：
```
Begin global transaction: [xid=xxx]
Rollback global transaction: [xid=xxx]
```
