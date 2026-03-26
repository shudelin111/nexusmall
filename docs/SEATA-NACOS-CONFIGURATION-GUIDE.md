# Seata Nacos 配置中心部署指南

## 📋 配置说明

本指南用于将 Seata 从 file 模式切换到 Nacos 配置中心和注册中心。

---

## ✅ 一、微服务 Client 端配置（已完成）

### 1.1 已修改的模块

所有微服务模块的 `application.yaml` 已更新为 Nacos 模式：

- ✅ `nexusmall-order`
- ✅ `nexusmall-auth`
- ✅ `nexusmall-product`
- ✅ `nexusmall-behavior`

### 1.2 关键配置参数说明

```yaml
seata:
  registry:
    type: nacos
    nacos:
      server-addr: 10.10.1.1:8848    # Nacos 服务器地址
      namespace: ""                   # 命名空间 ID（空表示 public）
      group: SEATA_GROUP              # 分组名称
      cluster: default                # 集群名称
      username: nacos                 # Nacos 用户名
      password: nacos                 # Nacos 密码
      application: seata-server       # Seata Server 在 Nacos 中的服务名
  
  config:
    type: nacos
    nacos:
      server-addr: 10.10.1.1:8848
      namespace: ""
      group: SEATA_GROUP
      username: nacos
      password: nacos
      data-id: seataServer.properties  # 配置文件名
```

---

## 🔧 二、Seata Server 端配置（需要执行）

### 2.1 准备工作

#### **步骤 1：初始化 Seata 数据库**

在 MySQL 中创建 `seata` 数据库并导入表结构：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS seata DEFAULT CHARACTER SET utf8mb4;

USE seata;

-- 导入 Seata 表结构（global_table, branch_table, lock_table）
-- 脚本位置：seata-server/script/server/db/mysql.sql
```

#### **步骤 2：下载 Seata Server**

下载地址：https://seata.apache.org/zh-cn/docs/v2.6/download/

推荐版本：**Seata 2.0.0+**

---

### 2.2 配置 Seata Server 使用 Nacos

#### **方式 A：修改 application.yml（推荐）**

编辑 `seata-server/conf/application.yml` 文件：

```yaml
server:
  port: 7091

spring:
  application:
    name: seata-server

logging:
  config: classpath:logback-spring.xml
  file:
    path: ${user.home}/logs/seata

console:
  user:
    username: seata
    password: seata

seata:
  # ========== 配置中心配置 ==========
  config:
    type: nacos
    nacos:
      server-addr: 10.10.1.1:8848
      namespace: ""                    # 命名空间 ID
      group: SEATA_GROUP
      username: nacos
      password: nacos
      data-id: seataServer.properties
  
  # ========== 注册中心配置 ==========
  registry:
    type: nacos
    nacos:
      application: seata-server        # 服务名（必须与 spring.application.name 一致）
      server-addr: 10.10.1.1:8848
      group: SEATA_GROUP
      namespace: ""
      cluster: default
      username: nacos
      password: nacos
  
  # ========== 存储模式配置 ==========
  store:
    mode: db                           # 使用数据库存储事务会话
    db:
      db-type: mysql
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://10.10.1.1:3306/seata?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
      user: root                       # 数据库用户名
      password: your_password          # 数据库密码
      min-conn: 5
      max-conn: 100
      global-table: global_table
      branch-table: branch_table
      lock-table: lock_table
      query-limit: 100
  
  # ========== 其他配置 ==========
  security:
    secretKey: SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
    tokenValidityInMilliseconds: 1800000
    ignore:
      urls: /,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/api/v1/auth/login
```

---

### 2.3 发布配置到 Nacos

#### **步骤 1：创建配置文件**

登录 Nacos 控制台：http://10.10.1.1:8848/nacos

1. 进入 "配置管理" -> "配置列表"
2. 点击 "+" 创建配置
3. 填写以下信息：
   - **Data ID**: `seataServer.properties`
   - **Group**: `SEATA_GROUP`
   - **描述**: Seata Server 全局配置
   - **格式**: `properties`

#### **步骤 2：配置内容**

在 Nacos 中添加以下配置内容：

```properties
#======================== Store Configuration ========================#
# 存储模式：db（数据库）
store.mode=db

# 数据库配置
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://10.10.1.1:3306/seata?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
store.db.user=root
store.db.password=your_password
store.db.minConn=5
store.db.maxConn=100
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.lockTable=lock_table
store.db.queryLimit=100

#======================== Transaction Configuration ========================#
# 全局事务回滚重试超时时间（毫秒）
seata.server.max-commit-retry-timeout=-1
seata.server.max-rollback-retry-timeout=-1

# 事务恢复配置
seata.server.recovery.handle-all-session-period=1000

# Undo 日志配置
seata.server.undo.logSaveDays=7
seata.server.undo.logDeletePeriod=86400000

#======================== Session Configuration ========================#
# 分支异步移除队列大小
seata.server.session.branchAsyncQueueSize=5000
seata.server.session.enableBranchAsyncRemove=false

#======================== Transport Configuration ========================#
# RPC 超时时间（毫秒）
seata.server.transport.rpcTcRequestTimeout=30000

#======================== Security Configuration ========================#
# JWT Token 密钥
seata.server.security.secretKey=SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
seata.server.security.tokenValidityInMilliseconds=1800000
```

---

## 🚀 三、启动 Seata Server

### 3.1 Windows 环境

```bash
cd D:\seata-server\bin
seata-server.bat
```

### 3.2 Linux 环境

```bash
cd /opt/seata-server/bin
./seata-server.sh
```

### 3.3 Docker 环境（可选）

```yaml
version: '3'
services:
  seata-server:
    image: apache/seata-server:2.0.0
    container_name: seata-server
    ports:
      - "7091:7091"
      - "8091:8091"
    environment:
      - SEATA_PORT=8091
      - STORE_MODE=db
      - SEATA_REGISTRY_TYPE=nacos
      - SEATA_CONFIG_TYPE=nacos
      - NACOS_SERVER_ADDR=10.10.1.1:8848
      - NACOS_NAMESPACE=""
      - NACOS_GROUP=SEATA_GROUP
      - NACOS_USERNAME=nacos
      - NACOS_PASSWORD=nacos
      - DB_URL=jdbc:mysql://10.10.1.1:3306/seata?useUnicode=true
      - DB_USER=root
      - DB_PASSWORD=your_password
    volumes:
      - ./logs:/root/logs/seata
```

---

## ✅ 四、验证配置

### 4.1 检查 Nacos 服务注册

访问 Nacos 控制台：http://10.10.1.1:8848/nacos

1. 进入 "服务管理" -> "服务列表"
2. 查看是否有 `seata-server` 服务
3. 确认健康实例数为 1

### 4.2 检查配置是否加载

访问 Seata 控制台：http://10.10.1.1:7091/seata

1. 登录：用户名 `seata` / 密码 `seata`
2. 进入 "事务组查询"
3. 查看是否能正常显示事务组信息

### 4.3 测试分布式事务

运行一个包含 `@GlobalTransactional` 注解的测试方法，查看：

1. Seata 控制台是否有事务记录
2. Nacos 配置是否生效
3. 数据库 undo_log 表是否有记录

---

## 🔍 五、常见问题排查

### 问题 1：Seata Server 无法注册到 Nacos

**症状**：Nacos 服务列表中没有 `seata-server`

**解决方案**：
1. 检查 `application.yml` 中 `registry.type` 是否为 `nacos`
2. 检查 `server-addr` 是否正确
3. 检查 Nacos 是否正常运行
4. 检查防火墙是否开放 8848 端口

### 问题 2：配置未从 Nacos 加载

**症状**：Seata 仍使用本地配置

**解决方案**：
1. 检查 `config.type` 是否为 `nacos`
2. 检查 `data-id` 是否与 Nacos 中的配置文件名一致
3. 检查 `group` 和 `namespace` 是否匹配
4. 重启 Seata Server

### 问题 3：数据库连接失败

**症状**：Seata 启动时报数据库连接错误

**解决方案**：
1. 检查 MySQL 是否正常运行
2. 检查 `seata` 数据库是否已创建
3. 检查数据库账号密码是否正确
4. 检查数据库表是否已导入

### 问题 4：微服务找不到 Seata Server

**症状**：微服务启动时报 "no provider found"

**解决方案**：
1. 确保 Seata Server 已成功注册到 Nacos
2. 检查微服务配置中 `registry.nacos.server-addr` 是否正确
3. 检查事务组映射 `vgroup-mapping` 是否正确
4. 重启微服务

---

## 📊 六、配置对比

| 配置项 | File 模式 | Nacos 模式 | 说明 |
|--------|----------|-----------|------|
| 注册中心 | file | nacos | 生产环境必须用 Nacos |
| 配置中心 | file | nacos | 集中管理配置 |
| 服务发现 | 静态配置 | 动态发现 | Nacos 自动感知服务上下线 |
| 配置更新 | 重启服务 | 热更新 | Nacos 支持配置实时推送 |
| 高可用 | 单点 | 集群 | Nacos 支持集群部署 |

---

## 🎯 七、下一步操作

### ✅ 已完成
1. ✅ 微服务 Client 端配置修改（4 个模块）
2. ✅ 配置参数统一化

### ⏳ 待执行
1. ⏳ 初始化 Seata 数据库表
2. ⏳ 下载并配置 Seata Server
3. ⏳ 在 Nacos 中创建配置文件
4. ⏳ 启动 Seata Server
5. ⏳ 验证配置是否生效
6. ⏳ 测试分布式事务

---

## 📚 八、参考文档

1. [Seata 官方文档](https://seata.apache.org/zh-cn/)
2. [Seata Nacos 配置指南](https://seata.apache.org/zh-cn/docs/user/configuration/)
3. [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
4. [Spring Cloud Alibaba 集成示例](https://github.com/alibaba/spring-cloud-alibaba)

---

**最后更新时间**：2026-03-26
