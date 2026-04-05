# OAuth2 JWT 认证升级 - 使用指南

## 📋 架构说明

```
客户端请求 → API Gateway (JWT 验证) → 微服务 (从 Header 读取用户信息)
```

- **网关层**: 统一验证 JWT Token (RS256)
- **微服务层**: 信任网关,从 Header 读取用户信息,不再重复验证 Token

---

## 🔧 在 Order/Product 微服务中使用

### **1. 获取当前用户信息**

```java
import com.nexusmall.common.context.UserContext;

@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @GetMapping("/my-orders")
    public Result<List<Order>> getMyOrders() {
        // 获取当前用户名
        String username = UserContext.getCurrentUsername();
        
        // 检查是否已登录
        if (!UserContext.isAuthenticated()) {
            return Result.failure("401", "未登录");
        }
        
        // 查询该用户的订单
        List<Order> orders = orderService.findByUsername(username);
        return Result.success(orders);
    }
}
```

### **2. 基于角色的权限控制**

```java
@PostMapping("/admin/orders")
public Result<Void> adminOperation() {
    // 检查是否有管理员角色
    if (!UserContext.hasRole("ADMIN")) {
        return Result.failure("403", "需要管理员权限");
    }
    
    // 执行管理员操作
    orderService.adminOperation();
    return Result.success();
}
```

### **3. 基于权限的细粒度控制**

```java
@DeleteMapping("/orders/{id}")
public Result<Void> deleteOrder(@PathVariable Long id) {
    // 检查是否有删除订单的权限
    if (!UserContext.hasPermission("order:delete")) {
        return Result.failure("403", "无删除权限");
    }
    
    orderService.deleteOrder(id);
    return Result.success();
}
```

### **4. 获取完整的用户信息**

```java
@GetMapping("/profile")
public Result<Map<String, Object>> getProfile() {
    Map<String, Object> profile = new HashMap<>();
    profile.put("username", UserContext.getCurrentUsername());
    profile.put("roles", UserContext.getCurrentRoles());
    profile.put("permissions", UserContext.getCurrentPermissions());
    
    return Result.success(profile);
}
```

---

## 🔗 Feign 微服务间调用

### **自动透传用户信息**

Feign 调用时会自动透传用户信息,无需手动处理:

```java
@FeignClient(name = "nexusmall-product")
public interface ProductFeignClient {
    
    @GetMapping("/products/{id}")
    Result<Product> getProduct(@PathVariable("id") Long id);
}

@Service
public class OrderService {
    
    @Autowired
    private ProductFeignClient productFeignClient;
    
    public void createOrder(Long productId) {
        // Feign 调用会自动携带用户信息
        Result<Product> result = productFeignClient.getProduct(productId);
        // Product 服务可以从 Header 中读取到用户信息
    }
}
```

---

## ⚙️ Nacos 配置

### **Auth 服务配置**

Data ID: `nexusmall-auth.yaml`

```yaml
security:
  jwt:
    rsa:
      private-key: |
        -----BEGIN PRIVATE KEY-----
        ... (私钥)
        -----END PRIVATE KEY-----
      public-key: |
        -----BEGIN PUBLIC KEY-----
        ... (公钥)
        -----END PUBLIC KEY-----
      access-token-expire-time: 1800000  # 30分钟
      refresh-token-expire-time: 604800000  # 7天
      issuer: nexusmall-auth
      audience: nexusmall-services
```

### **Gateway 配置**

Data ID: `nexusmall-gateway.yaml`

```yaml
security:
  jwt:
    rsa:
      public-key: |
        -----BEGIN PUBLIC KEY-----
        ... (与 auth 服务相同的公钥)
        -----END PUBLIC KEY-----
      issuer: nexusmall-auth
      audience: nexusmall-services
```

---

## 📝 API 接口说明

### **1. 用户登录**

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

响应:
```json
{
  "code": "10000",
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 1712345678901,
    "username": "admin",
    "roles": ["ADMIN", "USER"],
    "permissions": ["order:create", "order:view"]
  }
}
```

### **2. 刷新 Token**

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

响应:
```json
{
  "code": "10000",
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 1712345678901
  }
}
```

### **3. 用户登出**

```http
POST /auth/logout
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **4. 访问受保护的资源**

```http
GET /orders/my-orders
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🔒 安全特性

### **1. RS256 非对称加密**
- 私钥仅存在于 auth 服务
- 公钥分发给 gateway 和所有微服务
- 防止密钥泄露导致整个系统被攻破

### **2. Access Token + Refresh Token**
- Access Token: 短时效 (30分钟),用于 API 请求
- Refresh Token: 长时效 (7天),用于刷新 Access Token
- 降低 Token 泄露风险

### **3. Token 黑名单**
- 用户登出时,Token 立即加入 Redis 黑名单
- 支持即时撤销,解决 JWT 不可撤销的问题

### **4. 算法白名单**
- 强制验证 RS256 算法
- 防止算法混淆攻击

### **5. 标准 Claims 验证**
- 验证签发者 (iss)
- 验证受众 (aud)
- 验证过期时间 (exp)

---

## 🚀 迁移步骤

### **从旧版 JWT 迁移到新架构**

1. **更新依赖**
   ```xml
   <!-- 确保使用最新的 nexusmall-common -->
   <dependency>
       <groupId>com.nexusmall</groupId>
       <artifactId>nexusmall-common</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```

2. **移除旧的 JWT 验证代码**
   ```java
   // ❌ 删除这些代码
   @Autowired
   private JwtUtil jwtUtil;
   
   public void someMethod(String token) {
       boolean valid = jwtUtil.validateToken(token);
       // ...
   }
   ```

3. **使用 UserContext 获取用户信息**
   ```java
   // ✅ 改为这样
   String username = UserContext.getCurrentUsername();
   ```

4. **配置 Nacos**
   - 将 RSA 公钥配置添加到 Nacos
   - 重启服务

---

## 📊 性能优化建议

1. **网关缓存公钥**: 避免每次请求都解析 PEM 格式
2. **Redis 缓存用户信息**: 减少数据库查询
3. **异步清理过期 Token**: 定时任务清理 sys_refresh_token 表

---

## ❓ 常见问题

### **Q1: 为什么微服务不直接验证 JWT?**
A: 网关统一验证可以:
- 减少重复计算,提升性能
- 统一安全策略,便于管理
- 微服务专注于业务逻辑

### **Q2: 如果网关被绕过怎么办?**
A: 
- 内网隔离: 微服务只允许网关访问
- 防火墙规则: 限制外部直接访问微服务
- 可选: 微服务也配置 Resource Server 作为第二道防线

### **Q3: Token 刷新流程是什么?**
A:
1. Access Token 过期 (30分钟)
2. 前端使用 Refresh Token 调用 `/auth/refresh`
3. Auth 服务验证 Refresh Token
4. 返回新的 Access Token + Refresh Token
5. 旧的 Refresh Token 失效

---

## 📚 参考资料

- [OAuth 2.1 规范](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1)
- [Spring Authorization Server](https://spring.io/projects/spring-authorization-server)
- [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)
- [JSON Web Algorithms (JWA)](https://tools.ietf.org/html/rfc7518)
