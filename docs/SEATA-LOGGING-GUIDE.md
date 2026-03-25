# Seata 详细日志配置说明

## ✅ 已配置的日志级别

### 1. **核心日志** (DEBUG 级别)

| Logger 名称 | 作用 | 关键信息 |
|------------|------|----------|
| `io.seata` | Seata 总日志 | 所有 Seata 相关日志的根级别 |
| `io.seata.core.rpc` | RPC 通信日志 | 与 Seata Server 的网络通信 |
| `io.seata.core.protocol` | 协议日志 | TC、RM、TM 之间的消息协议 |
| `io.seata.rm` | 资源管理器日志 | 分支事务注册、汇报 |
| `io.seata.rm.datasource` | 数据源代理日志 | SQL 拦截和执行 |
| `io.seata.rm.datasource.undo` | **undo_log 日志** | **重点查看 undo_log 插入和删除** |
| `io.seata.tm` | 事务管理器日志 | 全局事务开始、提交、回滚 |
| `io.seata.spring.annotation` | 注解处理日志 | @GlobalTransactional 等注解处理 |
| `io.seata.sqlparser` | SQL 解析日志 | SQL 语义解析过程 |
| `io.seata.core.context` | 上下文日志 | XID 传播和上下文传递 |

---

## 🔍 关键日志解读

### **一阶段（Prepare）日志示例**

当你执行一个带 `@Transactional` 的方法时，会看到：

```log
# 1. 全局事务开始
io.seata.tm.TransactionManager : Begin a new global transaction, xid: 10.10.1.1:8091:2024032501

# 2. 分支事务注册
io.seata.rm.RMClient : Register a branch to TC, xid: 10.10.1.1:8091:2024032501, branchType: AT

# 3. SQL 解析
io.seata.sqlparser.AbstractSQLParser : Parse SQL: UPDATE product SET stock = stock - ? WHERE sku_id = ?

# 4. 查询前镜像
io.seata.rm.datasource.exec.SelectBeforeExecutor : Select before image: SELECT * FROM product WHERE sku_id = 1

# 5. 执行业务 SQL
io.seata.rm.datasource.exec.UpdateExecutor : Execute SQL: UPDATE product SET stock = stock - 1 WHERE sku_id = 1

# 6. 查询后镜像
io.seata.rm.datasource.exec.SelectAfterExecutor : Select after image: SELECT * FROM product WHERE sku_id = 1

# 7. ★★★ 插入 undo_log ★★★
io.seata.rm.datasource.undo.AbstractUndoLogManager : Insert undo_log record, xid: 10.10.1.1:8091:2024032501, branchId: 2024032502

# 8. 提交本地事务
io.seata.rm.datasource.ConnectionProxy : Commit local transaction, xid: 10.10.1.1:8091:2024032501

# 9. 向 TC 汇报
io.seata.rm.RMClient : Report branch status, xid: 10.10.1.1:8091:2024032501, branchId: 2024032502, status: PhaseOne_Done
```

---

### **二阶段提交日志示例**

如果全局事务成功，会看到：

```log
# 1. TC 通知提交
io.seata.tm.TransactionManager : Commit global transaction, xid: 10.10.1.1:8091:2024032501

# 2. RM 异步删除 undo_log
io.seata.rm.datasource.undo.AbstractUndoLogManager : Delete undo_log, xid: 10.10.1.1:8091:2024032501, branchId: 2024032502

# 3. 汇报提交结果
io.seata.rm.RMClient : Report branch commit success, xid: 10.10.1.1:8091:2024032501
```

---

### **二阶段回滚日志示例**

如果全局事务失败需要回滚，会看到：

```log
# 1. TC 通知回滚
io.seata.tm.TransactionManager : Rollback global transaction, xid: 10.10.1.1:8091:2024032501

# 2. RM 执行回滚
io.seata.rm.datasource.undo.AbstractUndoLogManager : Start rollback undo_log, xid: 10.10.1.1:8091:2024032501

# 3. 生成反向 SQL
io.seata.rm.datasource.undo.mysql.MySQLUndoLogManager : Generate reverse SQL from undo_log

# 4. 执行反向 SQL
io.seata.rm.datasource.undo.AbstractUndoLogExecutor : Execute reverse SQL: UPDATE product SET stock = 100 WHERE sku_id = 1

# 5. 删除 undo_log
io.seata.rm.datasource.undo.AbstractUndoLogManager : Delete undo_log after rollback, xid: 10.10.1.1:8091:2024032501

# 6. 汇报回滚结果
io.seata.rm.RMClient : Report branch rollback success, xid: 10.10.1.1:8091:2024032501
```

---

## 🎯 重点关注日志

### **查看 undo_log 插入**
搜索关键字：
```
Insert undo_log record
```

### **查看 SQL 执行详情**
搜索关键字：
```
Execute SQL:
Select before image:
Select after image:
```

### **查看 XID 传播**
搜索关键字：
```
xid:
```

### **查看事务状态变化**
搜索关键字：
```
Begin a new global transaction
Commit global transaction
Rollback global transaction
```

---

## 📊 日志级别对比

| 级别 | 输出内容 | 适用场景 |
|------|---------|----------|
| **TRACE** | 最详细，包含所有方法调用 | 深度调试（不推荐日常使用） |
| **DEBUG** ✅ | 详细执行过程、SQL、undo_log | **开发调试（当前配置）** |
| **INFO** | 关键节点、事务开始/结束 | 生产环境 |
| **WARN** | 警告信息 | 生产环境 |
| **ERROR** | 错误信息 | 生产环境 |

---

## ⚠️ 注意事项

### **1. 日志量很大**
开启 DEBUG 后，每次事务操作会产生 50+ 条日志，建议：
- 只在开发环境使用
- 测试完成后调回 INFO 级别

### **2. 性能影响**
DEBUG 级别会影响性能（约 10-20%），不适合生产环境。

### **3. 磁盘空间**
日志文件增长很快，注意定期清理或设置合理的滚动策略。

---

## 🔧 快速调整日志级别

### **临时调整（仅当前运行有效）**

在应用运行时，通过 JMX 或 Spring Boot Actuator 调整：

```bash
# 调整为 INFO 级别
curl -X POST http://localhost:11000/actuator/loggers/io.seata \
  -H 'Content-Type: application/json' \
  -d '{"configuredLevel":"INFO"}'
```

### **永久调整**

修改 `application.yaml`：

```yaml
logging:
  level:
    io.seata: INFO  # 改为 INFO
```

---

## 📝 测试建议

### **测试正常流程**
1. 启动服务
2. 执行正常订单创建
3. 观察日志中的 `PhaseOne_Done` 和 `Commit` 记录

### **测试回滚流程**
1. 在 `OrderServiceImpl.createOrder()` 中保持测试代码：
   ```java
   if (true) {
       throw new RuntimeException("测试回滚");
   }
   ```
2. 执行订单创建
3. 观察日志中的 `Rollback` 和 `reverse SQL` 记录

### **验证 undo_log**
执行 SQL 查看：
```sql
-- 查看最近的 undo_log
SELECT id, xid, branch_id, log_status, log_created 
FROM undo_log 
ORDER BY log_created DESC 
LIMIT 5;

-- 应该在日志中看到对应的 INSERT 语句
```

---

## ✨ 总结

当前配置已开启 Seata 的所有关键日志，包括：
- ✅ 全局事务管理（TM）
- ✅ 资源管理（RM）
- ✅ SQL 解析和执行
- ✅ **undo_log 插入和删除** ← 重点关注
- ✅ RPC 通信
- ✅ XID 传播

重启微服务后，所有 Seata 操作的详细日志都会输出到：
- 控制台（dev 环境）
- `logs/nexusmall-xxx/` 目录下的日志文件

需要我帮你重启服务并查看日志吗？
