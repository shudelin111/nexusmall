# Product 模块快速开始指南

## 一、环境准备

### 1. 数据库配置
确保 MySQL 数据库已启动，并创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS nexusmall_product 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;
```

### 2. 执行表结构脚本
执行 `product-table.sql` 文件创建表和测试数据：

```bash
mysql -u root -p nexusmall_product < product-table.sql
```

### 3. Seata Server 配置
参考 `seata-config.txt` 配置并启动 Seata Server：

```bash
cd seata-1.5.2
bin\seata-server.bat  # Windows
```

## 二、配置修改

### 1. 数据库连接配置
修改 `nexusmall-product/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nexusmall_product?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. Redis 配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password  # 如果有密码
```

### 3. Nacos 配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

### 4. Seata 配置
确保 Seata 配置正确：

```yaml
seata:
  service:
    grouplist:
      default: localhost:8091
```

## 三、启动服务

### 1. 编译项目
```bash
cd nexusmall-product
mvn clean install
```

### 2. 启动服务
```bash
mvn spring-boot:run
```

或直接运行 `NexusmallProductApplication.java`

### 3. 验证启动成功
访问健康检查接口：
```bash
curl http://localhost:10000/product/ping
```

预期返回：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "service": "nexusmall-product",
    "status": "UP",
    "message": "product service is ready",
    "redis": "ok"
  }
}
```

## 四、API 测试

### 1. 查询所有商品
```bash
curl http://localhost:10000/product/list
```

### 2. 查询单个商品
```bash
curl http://localhost:10000/product/1
```

### 3. 按条件查询
```bash
curl "http://localhost:10000/product/listByCondition?keyword=华为&status=1"
```

### 4. 扣减库存（分布式事务）
```bash
curl -X POST "http://localhost:10000/product/decreaseStock?productId=1&count=1"
```

### 5. 增加库存（分布式事务）
```bash
curl -X POST "http://localhost:10000/product/increaseStock?productId=1&count=1"
```

### 6. 批量扣减库存
```bash
curl -X POST "http://localhost:10000/product/batchDecreaseStock" \
  -H "Content-Type: application/json" \
  -d '[{"skuId":1,"count":1},{"skuId":2,"count":2}]'
```

### 7. 上架商品
```bash
curl -X PUT "http://localhost:10000/product/putOnSale/1"
```

### 8. 下架商品
```bash
curl -X PUT "http://localhost:10000/product/putOffSale/1"
```

### 9. 分类接口测试
```bash
# 查询所有一级分类
curl http://localhost:10000/category/firstLevel

# 查询指定父级的子分类
curl http://localhost:10000/category/listByParentId/1
```

### 10. 品牌接口测试
```bash
# 查询所有品牌
curl http://localhost:10000/brand/list

# 根据首字母查询品牌
curl http://localhost:10000/brand/listByFirstLetter/H
```

## 五、集成测试

### 1. 测试订单创建流程（跨服务分布式事务）

在 order 服务中调用：

```java
@Autowired
private ProductFeignService productFeignService;

@GlobalTransactional
public void createOrder(Order order) {
    // 扣减库存
    productFeignService.decreaseStock(order.getProductId(), order.getCount());
    
    // 创建订单
    orderMapper.insert(order);
}
```

### 2. 测试回滚

故意制造库存不足的场景：

```bash
# 尝试扣减超过库存数量的商品
curl -X POST "http://localhost:10000/product/decreaseStock?productId=1&count=9999"
```

应该返回错误，并且数据应该回滚。

## 六、常见问题排查

### 1. 数据库连接失败
- 检查 MySQL 是否启动
- 检查数据库名、用户名、密码是否正确
- 检查网络连接

### 2. Redis 连接失败
- 检查 Redis 是否启动
- 检查 Redis 密码是否正确

### 3. Seata 事务不生效
- 检查 Seata Server 是否启动
- 检查 `seata-service-group` 配置是否一致
- 检查 Feign 客户端是否配置了 fallback

### 4. Feign 调用失败
- 检查服务是否在 Nacos 注册
- 检查服务名是否正确
- 检查网络是否通畅

### 5. MyBatis Mapper 未找到
- 检查 XML 文件位置是否正确
- 检查 `mapper-locations` 配置
- 检查 Mapper 接口和 XML 的 namespace 是否匹配

## 七、开发建议

### 1. 代码规范
- Controller 层只处理 HTTP 请求和响应
- Service 层处理业务逻辑
- DAO 层只负责数据库操作

### 2. 事务使用
- 涉及跨服务调用时使用 `@GlobalTransactional`
- 本地事务使用 `@Transactional`
- 明确指定 `rollbackFor = Exception.class`

### 3. 异常处理
- 使用统一的全局异常处理器
- 自定义业务异常类
- 记录详细的日志信息

### 4. 性能优化
- 使用连接池
- 配置合理的线程池
- 考虑使用缓存

## 八、下一步

1. 实现商品搜索功能（Elasticsearch）
2. 添加商品评论功能
3. 实现商品收藏功能
4. 添加商品图片上传功能
5. 实现商品审核流程
