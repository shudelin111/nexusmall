# Sentinel 集成使用指南

## 一、已完成的工作

### 1.1 依赖添加
✅ 父工程 pom.xml 添加 Sentinel 依赖管理
✅ nexusmall-product 模块添加 Sentinel 依赖
✅ nexusmall-order 模块添加 Sentinel 依赖
✅ nexusmall-auth 模块添加 Sentinel 依赖
✅ nexusmall-behavior 模块添加 Sentinel 依赖

### 1.2 配置修改
✅ 所有模块 application.yaml 添加 Sentinel 配置
✅ 启用 Feign Sentinel 熔断
✅ 配置 Nacos 数据源（用于规则持久化）

### 1.3 编译验证
✅ 所有 8 个模块编译成功

---

## 二、部署 Sentinel Dashboard

### 2.1 运行部署脚本
```powershell
# 在项目根目录执行
.\deploy-sentinel-dashboard.ps1
```

### 2.2 访问控制台
- **访问地址**：http://10.10.1.1:8080
- **用户名**：sentinel
- **密码**：sentinel

---

## 三、启动微服务

部署 Dashboard 后，启动微服务即可自动注册到 Sentinel：

```powershell
# 启动商品服务
cd nexusmall-product
mvn spring-boot:run

# 启动订单服务
cd nexusmall-order
mvn spring-boot:run

# 启动认证服务
cd nexusmall-auth
mvn spring-boot:run

# 启动行为服务
cd nexusmall-behavior
mvn spring-boot:run
```

---

## 四、配置限流规则（示例）

### 4.1 商品查询限流
**场景**：防止恶意刷商品详情接口

1. 登录 Sentinel Dashboard
2. 选择 `nexusmall-product` 服务
3. 点击"流控规则" → "新增流控规则"
4. 配置如下：
   - **资源名**：`GET /api/product/{skuId}`
   - **针对来源**：default
   - **阈值类型**：QPS
   - **单机阈值**：100（每秒最多 100 次请求）
   - **流控模式**：直接
   - **流控效果**：快速失败

**效果**：超过 100 QPS 的请求会立即返回 429 错误

---

### 4.2 订单创建限流 + 降级
**场景**：保护订单服务不被流量洪峰打垮

1. 选择 `nexusmall-order` 服务
2. 点击"流控规则" → "新增流控规则"
3. 配置如下：
   - **资源名**：`createOrder`（@SentinelResource 注解的资源名）
   - **阈值类型**：QPS
   - **单机阈值**：50
   - **流控模式**：直接
   - **流控效果**：Warm Up（预热模式）
   - **预热时长**：10 秒

**效果**：系统从低水位缓慢提升放行速率，避免冷启动打垮系统

---

### 4.3 Feign 客户端熔断
**场景**：Order 服务调用 Product 服务失败率过高时自动熔断

1. 选择 `nexusmall-order` 服务
2. 点击"熔断降级" → "新增熔断规则"
3. 配置如下：
   - **资源名**：`GET http://nexusmall-product/api/product/{skuId}`
   - **熔断策略**：慢调用比例
   - **最大 RT**：3000ms
   - **熔断比例**：50%
   - **熔断时长**：10s
   - **最小请求数**：10

**效果**：
- 当 Product 服务响应时间超过 3 秒的比例达到 50% 时触发熔断
- 熔断后 10 秒内不再调用 Product 服务
- 直接返回降级兜底数据

---

## 五、代码中使用 @SentinelResource

### 5.1 OrderController 示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单（限流 + 熔断 + 降级三合一）
     */
    @PostMapping("/create")
    @SentinelResource(
        value = "createOrder",              // 资源名
        blockHandler = "createOrderBlock",  // 限流/熔断时的兜底方法
        fallback = "createOrderFallback"    // 服务异常时的兜底方法
    )
    public Result<OrderVO> createOrder(@RequestBody OrderDTO dto) {
        return Result.success(orderService.createOrder(dto));
    }
    
    /**
     * 限流/熔断兜底方法（参数要和原方法一致，最后加 BlockException）
     */
    public Result<OrderVO> createOrderBlock(OrderDTO dto, BlockException e) {
        log.warn("【限流触发】订单创建被限流，userId: {}", dto.getUserId());
        return Result.fail("系统繁忙，请稍后再试（限流保护中）");
    }
    
    /**
     * 异常降级兜底方法（参数和返回值要和原方法一致）
     */
    public Result<OrderVO> createOrderFallback(OrderDTO dto, Throwable e) {
        log.error("【服务异常】订单创建失败，userId: {}", dto.getUserId(), e);
        return Result.fail("订单创建失败，请重试");
    }
}
```

---

## 六、监控指标说明

### 6.1 核心指标
- **QPS**：每秒请求数
- **RT**：平均响应时间（毫秒）
- **通过请求数**：成功放行的请求数
- **拦截请求数**：被限流/熔断拦截的请求数
- **异常数**：业务异常次数
- **异常率**：异常请求占比

### 6.2 查看监控
1. 登录 Sentinel Dashboard
2. 点击"集群监控" → 选择服务
3. 实时查看 QPS、RT 曲线图

---

## 七、规则持久化（生产环境必备）

### 7.1 为什么需要持久化？
- Dashboard 配置的规则存储在内存中
- Dashboard 重启后规则丢失
- 生产环境需要将规则存储到 Nacos

### 7.2 已配置 Nacos 数据源
项目中已配置 Nacos 数据源，规则会自动存储到 Nacos：

```yaml
spring:
  cloud:
    sentinel:
      datasource:
        ds1:
          nacos:
            server-addr: 10.10.1.1:8848
            dataId: ${spring.application.name}-sentinel
            groupId: DEFAULT_GROUP
            rule-type: flow
```

### 7.3 在 Nacos 中配置规则
1. 登录 Nacos 控制台（http://10.10.1.1:8848/nacos）
2. 进入"配置列表"
3. 找到 Data ID 为 `nexusmall-product-sentinel` 的配置
4. 添加流控规则 JSON：

```json
[
  {
    "resource": "GET /api/product/{skuId}",
    "limitApp": "default",
    "grade": 1,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

**字段说明**：
- `grade`: 限流类型（1=QPS, 0=线程数）
- `count`: 阈值
- `controlBehavior`: 流控效果（0=快速失败，1=Warm Up，2=匀速排队）

---

## 八、生产环境建议

### 8.1 必配的 3 条保命规则

#### （1）核心接口限流
```
资源：POST /api/order/create
QPS 阈值：500
流控效果：Warm Up（预热 10 秒）
```

#### （2）Feign 客户端熔断
```
资源：GET http://nexusmall-product/api/product/*
熔断策略：慢调用比例
最大 RT：3000ms
熔断比例：50%
熔断时长：10s
```

#### （3）系统保护规则
```
CPU 使用率：70%
系统 Load：CPU 核数 * 0.7
内存使用率：80%
```

### 8.2 规则调优建议
1. **压测定阈值**：通过压力测试确定系统真实承载能力
2. **灰度调整**：从低阈值开始，逐步上调到合理值
3. **持续监控**：根据实际运行情况动态调整规则
4. **分级保护**：
   - Gateway 层：整体限流（保护整个系统）
   - Service 层：精细化限流（保护单个服务）
   - Feign 层：熔断降级（保护下游服务）

---

## 九、常见问题

### Q1：Sentinel Dashboard 无法访问？
**检查**：
1. Docker 容器是否运行：`docker ps | grep sentinel-dashboard`
2. 端口是否被占用：`netstat -ano | findstr :8080`
3. 防火墙是否开放 8080 端口

### Q2：微服务没有注册到 Sentinel？
**检查**：
1. `application.yaml` 中 Sentinel 配置是否正确
2. `spring.cloud.sentinel.transport.dashboard` 地址是否正确
3. 微服务是否成功启动

### Q3：规则配置后不生效？
**检查**：
1. 资源名是否与代码中一致（区分大小写）
2. 是否配置到了正确的服务
3. 查看 Sentinel 日志：`docker logs sentinel-dashboard`

---

## 十、总结

### 已完成：
✅ Sentinel 依赖添加
✅ 配置文件修改
✅ Feign 熔断启用
✅ Nacos 数据源配置
✅ 编译验证通过
✅ Docker 部署脚本

### 下一步：
1. 运行 `.\deploy-sentinel-dashboard.ps1` 部署 Dashboard
2. 启动微服务
3. 登录 Dashboard 配置限流、熔断规则
4. 实时监控服务运行状态

### 生产环境：
- 使用 Nacos 持久化规则
- 配置告警通知
- 定期压测调优
- 建立值班响应机制

---

**祝部署顺利！** 🎉
