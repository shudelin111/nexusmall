# NexusMall Nacos 配置部署清单

## 📦 第一步：在 Nacos 创建公共配置

### 1. nexusmall-common.yaml（平台公共配置）

**访问 Nacos 控制台：** `http://10.10.1.1:8848/nacos`  
**用户名：** `nacos`  **密码：** `nacos`

**配置信息：**
- **Data ID:** `nexusmall-common.yaml`
- **Group:** `DEFAULT_GROUP`
- **命名空间:** `public`（默认）
- **配置格式:** `YAML`

**配置内容：** 复制 `docs/nacos-configs/nexusmall-common.yaml` 的全部内容

---

### 2. nexusmall-datasource.yaml（数据库连接池配置）

**配置信息：**
- **Data ID:** `nexusmall-datasource.yaml`
- **Group:** `DEFAULT_GROUP`
- **命名空间:** `public`
- **配置格式:** `YAML`

**配置内容：** 复制 `docs/nacos-configs/nexusmall-datasource.yaml` 的全部内容

---

### 3. nexusmall-order.yaml（订单服务专属配置）

**配置信息：**
- **Data ID:** `nexusmall-order.yaml`
- **Group:** `DEFAULT_GROUP`
- **命名空间:** `public`
- **配置格式:** `YAML`

**配置内容：** 复制 `docs/nacos-configs/nexusmall-order.yaml` 的全部内容

**⚠️ 重要：** 修改数据库连接信息
```yaml
spring:
  datasource:
    url: jdbc:mysql://10.10.1.1:3306/nexusmall_order?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: "123456"  # 修改为实际密码
```

---

## 📝 第二步：修改本地配置

### Order 模块配置已修改

**文件：** `nexusmall-order/src/main/resources/application.yaml`

**修改内容：**
1. ✅ 添加了三个 Nacos 配置导入
2. ✅ 删除了重复的数据库配置
3. ✅ 删除了重复的 Redis 配置
4. ✅ 删除了重复的服务器端口配置
5. ✅ 删除了重复的 MyBatis 配置
6. ✅ 删除了重复的 RocketMQ 配置
7. ✅ 删除了重复的 Seata 配置
8. ✅ 删除了重复的日志配置

**当前配置导入顺序：**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"      # 1. 平台公共配置
      - "optional:nacos:nexusmall-datasource.yaml"  # 2. 数据库连接池配置
      - "optional:nacos:nexusmall-order.yaml"       # 3. 服务专属配置
```

---

## ✅ 第三步：验证配置

### 1. 重启 Order 服务

```powershell
cd D:\IdeaProjects\nexusmall\nexusmall-order
mvn clean spring-boot:run
```

### 2. 查看启动日志

应该能看到：
```log
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-0'}]
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-1'}]
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-2'}]
```

说明成功加载了 3 个配置源。

### 3. 测试接口

```powershell
# 调用 Order 接口
Invoke-RestMethod -Uri "http://localhost:11000/order/1" -Method Get

# 查看是否使用了 Nacos 中的配置
```

---

## 📋 配置优先级说明

当多个配置文件中存在相同的配置项时，优先级如下：

```
1. nexusmall-order.yaml（服务专属配置）          ← 最高优先级
   ↓
2. nexusmall-datasource.yaml（数据库配置）
   ↓
3. nexusmall-common.yaml（平台公共配置）         ← 最低优先级
```

**示例：**
- 如果 3 个文件都配置了 `logging.level.root`
  - common.yaml: `INFO`
  - datasource.yaml: `DEBUG`
  - order.yaml: `WARN`
  
  **最终生效：** `WARN`（服务专属配置优先级最高）

---

## 🎯 其他服务配置参考

### Product 服务

**需要创建的 Nacos 配置：**
1. `nexusmall-common.yaml` ✅（已创建，所有服务共用）
2. `nexusmall-datasource.yaml` ✅（已创建，需要数据库的服务共用）
3. `nexusmall-product.yaml`（参考 order.yaml 创建）

**本地配置修改：**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-product.yaml"
```

---

### Auth 服务

**需要创建的 Nacos 配置：**
1. `nexusmall-common.yaml` ✅
2. `nexusmall-datasource.yaml` ✅
3. `nexusmall-auth.yaml`（参考 order.yaml 创建）

**本地配置修改：**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-auth.yaml"
```

---

### Gateway 服务

**需要创建的 Nacos 配置：**
1. `nexusmall-common.yaml` ✅
2. `nexusmall-gateway.yaml`（不需要数据库配置）

**本地配置修改：**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-gateway.yaml"
```

---

## 🔐 敏感信息处理

### 生产环境建议使用环境变量

**Nacos 配置中：**
```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}  # 从环境变量读取
```

**启动命令：**
```bash
java -jar nexusmall-order.jar \
  --DB_PASSWORD=your_password \
  --REDIS_PASSWORD=your_redis_password
```

---

## 📚 参考文档

- 详细配置规范：`docs/NACOS-CONFIG-DEPLOYMENT-GUIDE.md`
- Spring Cloud Alibaba 官方文档
- Nacos 官方文档

---

**最后更新：** 2026-03-27  
**维护团队：** NexusMall 技术团队
