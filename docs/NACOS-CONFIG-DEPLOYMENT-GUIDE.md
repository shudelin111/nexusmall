# NexusMall Nacos 配置中心部署指南

## 📋 配置架构设计

### 配置分层（优先级从高到低）

```
┌─────────────────────────────────────┐
│  1. 服务专属配置（最高优先级）        │
│     nexusmall-{service}.yaml        │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  2. 数据库连接池配置（共享）          │
│     nexusmall-datasource.yaml       │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  3. 平台公共配置（最低优先级）        │
│     nexusmall-common.yaml           │
└─────────────────────────────────────┘
```

---

##  第一步：在 Nacos 控制台创建配置

### 1.1 登录 Nacos 控制台

**访问地址：** `http://10.10.1.1:8848/nacos`  
**用户名：** `nacos`  
**密码：** `nacos`

---

### 1.2 创建公共配置（所有服务都需要）

#### **配置 1：nexusmall-common.yaml**

- **Data ID:** `nexusmall-common.yaml`
- **Group:** `DEFAULT_GROUP`
- **命名空间:** `public`（下拉选择默认）
- **配置格式:** `YAML`
- **配置内容:** 复制 `docs/nacos-configs/nexusmall-common.yaml` 的内容

**操作步骤：**
1. 点击左侧菜单 **配置管理** → **配置列表**
2. 点击 **+** 按钮创建配置
3. 填写上述信息
4. 粘贴配置内容
5. 点击 **发布**

---

#### **配置 2：nexusmall-datasource.yaml**

- **Data ID:** `nexusmall-datasource.yaml`
- **Group:** `DEFAULT_GROUP`
- **命名空间:** `public`
- **配置格式:** `YAML`
- **配置内容:** 复制 `docs/nacos-configs/nexusmall-datasource.yaml` 的内容

---

### 1.3 创建服务专属配置

为每个服务创建专属配置：

| 服务名称 | Data ID | 端口 |
|---------|---------|------|
| **认证服务** | `nexusmall-auth.yaml` | 8000 |
| **商品服务** | `nexusmall-product.yaml` | 10000 |
| **订单服务** | `nexusmall-order.yaml` | 11000 |
| **行为服务** | `nexusmall-behavior.yaml` | 12000 |
| **第三方服务** | `nexusmall-third-party.yaml` | 13000 |
| **网关服务** | `nexusmall-gateway.yaml` | 8080 |

**配置内容参考：** `docs/nacos-configs/nexusmall-order.yaml`（模板）

---

## 📦 第二步：修改本地配置文件

### 2.1 修改 Order 服务配置示例

**文件：** `nexusmall-order/src/main/resources/application.yaml`

**修改前：**
```yaml
spring:
  config:
    import: "optional:nacos:${spring.application.name}.${spring.cloud.nacos.config.file-extension}"
```

**修改后（支持多层配置）：**
```yaml
spring:
  config:
    import:
      # 1. 平台公共配置（所有服务共享）
      - "optional:nacos:nexusmall-common.yaml"
      # 2. 数据库连接池配置（需要数据库的服务都引入）
      - "optional:nacos:nexusmall-datasource.yaml"
      # 3. 服务专属配置（最高优先级）
      - "optional:nacos:${spring.application.name}.${spring.cloud.nacos.config.file-extension}"
```

---

### 2.2 所有服务的配置导入列表

#### **Order 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-order.yaml"
```

#### **Product 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-product.yaml"
```

#### **Auth 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-auth.yaml"
```

#### **Behavior 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-datasource.yaml"
      - "optional:nacos:nexusmall-behavior.yaml"
```

#### **Third-Party 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-third-party.yaml"
```
（不需要数据库配置）

#### **Gateway 服务**
```yaml
spring:
  config:
    import:
      - "optional:nacos:nexusmall-common.yaml"
      - "optional:nacos:nexusmall-gateway.yaml"
```
（不需要数据库配置）

---

## 🔐 第三步：敏感信息处理（生产环境）

### **方案 1：使用 Nacos 加密配置**

**步骤：**
1. 在 Nacos 配置中使用加密占位符
2. 通过环境变量或启动参数传入真实值

**示例：**
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

### **方案 2：使用环境变量**

**Docker 部署示例：**
```yaml
version: '3'
services:
  order-service:
    image: nexusmall-order:latest
    environment:
      - DB_PASSWORD=your_password
      - REDIS_PASSWORD=your_redis_password
```

---

## ✅ 第四步：验证配置是否生效

### **4.1 查看启动日志**

启动服务后，查看日志：

```log
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-0'}]
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-1'}]
INFO  o.s.c.b.c.PropertySourceBootstrapConfiguration - Located property source: [BootstrapPropertySource {name='bootstrapProperties-nacos-config-2'}]
```

说明成功加载了 3 个配置源。

---

### **4.2 访问 Actuator 端点验证**

**访问：** `http://localhost:11000/actuator/env`

查看配置属性，应该能看到来自 Nacos 的配置。

---

### **4.3 配置热更新测试**

1. 在 Nacos 控制台修改 `nexusmall-common.yaml`
2. 添加或修改配置项
3. 点击 **发布**
4. 查看服务日志，应该能看到配置刷新日志

```log
INFO  o.s.c.e.event.RefreshEvent - Refreshing environment properties...
```

---

## 📊 配置优先级说明

当多个配置文件中存在相同的配置项时，优先级如下：

```
1. 服务专属配置（nexusmall-order.yaml）          ← 最高优先级
   ↓
2. 数据库连接池配置（nexusmall-datasource.yaml）
   ↓
3. 平台公共配置（nexusmall-common.yaml）         ← 最低优先级
```

**示例：**

如果 3 个文件都配置了 `logging.level.root`：
- `nexusmall-common.yaml`: `INFO`
- `nexusmall-datasource.yaml`: `DEBUG`
- `nexusmall-order.yaml`: `WARN`

**最终生效：** `WARN`（服务专属配置优先级最高）

---

## 🎯 业界最佳实践

### ✅ **推荐做法**

1. **配置分层** - 公共配置、业务配置分离
2. **环境隔离** - dev/test/prod 使用不同命名空间
3. **敏感加密** - 密码等敏感信息使用环境变量
4. **版本管理** - 配置变更要有版本记录
5. **灰度发布** - 使用 Beta 发布功能先小范围测试

### ❌ **避免做法**

1. ❌ 所有配置写在一个文件中
2. ❌ 硬编码密码等敏感信息
3. ❌ 生产环境和开发环境共用配置
4. ❌ 配置项命名不规范
5. ❌ 没有配置说明文档

---

## 📋 配置清单检查表

### 公共配置（nexusmall-common.yaml）
- [ ] 日志配置
- [ ] Actuator 监控
- [ ] Swagger 文档
- [ ] Jackson 序列化
- [ ] HTTP 配置
- [ ] 文件上传配置

### 数据库配置（nexusmall-datasource.yaml）
- [ ] HikariCP 连接池参数
- [ ] 连接超时配置
- [ ] JMX 监控配置

### 服务专属配置（nexusmall-{service}.yaml）
- [ ] 服务名称
- [ ] 服务端口
- [ ] 数据库连接（需要数据库的服务）
- [ ] Redis 配置
- [ ] MyBatis-Plus 配置
- [ ] OpenFeign 配置
- [ ] RocketMQ 配置
- [ ] Seata 配置（需要分布式事务的服务）
- [ ] 业务特定配置

---

## 🚀 快速部署脚本

### PowerShell 批量导入脚本（可选）

```powershell
# 导入 Nacos 配置的 PowerShell 脚本
$nacosServer = "http://10.10.1.1:8848"
$username = "nacos"
$password = "nacos"

# 登录获取 token
$loginBody = @{ username = $username; password = $password }
$loginResponse = Invoke-RestMethod -Uri "$nacosServer/nacos/v1/auth/users/login" -Method Post -Body $loginBody
$token = $loginResponse.accessToken

# 配置文件列表
$configFiles = @(
    "nexusmall-common.yaml",
    "nexusmall-datasource.yaml",
    "nexusmall-order.yaml",
    "nexusmall-product.yaml",
    "nexusmall-auth.yaml",
    "nexusmall-behavior.yaml",
    "nexusmall-third-party.yaml",
    "nexusmall-gateway.yaml"
)

# 批量导入
foreach ($configFile in $configFiles) {
    $content = Get-Content "docs\nacos-configs\$configFile" -Raw
    $dataId = $configFile
    
    # 调用 Nacos API 导入配置
    $headers = @{ "accessToken" = $token }
    $body = @{
        dataId = $dataId
        group = "DEFAULT_GROUP"
        content = $content
        type = "yaml"
    }
    
    Invoke-RestMethod -Uri "$nacosServer/nacos/v1/cs/configs" -Method Post -Headers $headers -Body $body
    Write-Host "✅ 导入配置：$dataId"
}
```

---

## 📚 参考文档

- [Spring Cloud Alibaba 官方文档 - Nacos Config](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/zh-cn/index.html#_nacos_config)
- [Nacos 官方文档 - 配置管理](https://nacos.io/zh-cn/docs/quick-start.html)
- [阿里巴巴微服务配置规范](https://github.com/alibaba/spring-cloud-alibaba/wiki)

---

## 💡 常见问题

### Q1: 配置不生效怎么办？

**排查步骤：**
1. 检查 Nacos 连接是否正常
2. 查看启动日志是否有配置加载信息
3. 检查配置导入顺序是否正确
4. 访问 `/actuator/env` 查看配置是否加载

### Q2: 如何实现配置热更新？

**方法：**
1. 在代码中添加 `@RefreshScope` 注解
2. Nacos 配置变更发布后自动刷新

**示例：**
```java
@RestController
@RefreshScope
public class ConfigController {
    
    @Value("${some.config:default}")
    private String someConfig;
}
```

### Q3: 本地开发和生产环境如何切换？

**方案：**
1. 使用 Nacos 命名空间隔离（dev/test/prod）
2. 通过启动参数指定：
```bash
java -jar app.jar --spring.cloud.nacos.config.namespace=prod
```

---

**最后更新：** 2026-03-27  
**维护团队：** NexusMall 技术团队
