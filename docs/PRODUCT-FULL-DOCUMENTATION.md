# NexusMall 商品模块完整开发文档

## 📦 模块概述

Product 模块是 NexusMall 电商系统的核心商品管理服务，提供商品、分类、品牌的完整 CRUD 操作，以及库存管理功能。支持分布式事务，确保数据一致性。

## 🏗️ 架构设计

### 技术栈
- **Spring Boot 2.7.18** - 基础框架
- **Spring Cloud Alibaba 2021.0.5.0** - 微服务框架
- **Seata 1.5.2** - 分布式事务
- **MyBatis Plus 3.5.3.1** - ORM 框架
- **Nacos** - 服务注册与配置中心
- **OpenFeign** - 服务调用
- **Redis** - 缓存
- **MySQL 8.0** - 数据库

### 模块结构
```
nexusmall-product/
├── src/main/java/com/nexusmall/product/
│   ├── config/                          # 配置类
│   │   ├── FeignConfig.java            # Feign 配置
│   │   └── .gitkeep
│   ├── controller/                      # 控制器层
│   │   ├── ProductController.java      # 商品控制器
│   │   ├── CategoryController.java     # 分类控制器
│   │   ├── BrandController.java        # 品牌控制器
│   │   └── .gitkeep
│   ├── dao/                             # 数据访问层
│   │   ├── ProductMapper.java          # 商品 Mapper
│   │   ├── CategoryMapper.java         # 分类 Mapper
│   │   ├── BrandMapper.java            # 品牌 Mapper
│   │   ├── ProductStockDTO.java        # 库存 DTO
│   │   └── .gitkeep
│   ├── entity/                          # 实体类
│   │   ├── Product.java                # 商品实体
│   │   ├── Category.java               # 分类实体
│   │   ├── Brand.java                  # 品牌实体
│   │   └── .gitkeep
│   ├── exception/                       # 异常处理
│   │   ├── ProductNotFoundException.java  # 商品未找到异常
│   │   ├── GlobalExceptionHandler.java    # 全局异常处理
│   │   └── .gitkeep
│   ├── feign/                           # Feign 客户端
│   │   ├── ProductFeignService.java    # Feign 接口
│   │   ├── ProductFeignFallback.java   # 降级处理
│   │   └── .gitkeep
│   ├── service/                         # 服务层
│   │   ├── ProductService.java         # 商品服务接口
│   │   ├── CategoryService.java        # 分类服务接口
│   │   ├── BrandService.java           # 品牌服务接口
│   │   ├── impl/                       # 实现类
│   │   │   ├── ProductServiceImpl.java
│   │   │   ├── CategoryServiceImpl.java
│   │   │   ├── BrandServiceImpl.java
│   │   │   └── .gitkeep
│   │   └── .gitkeep
│   ├── vo/                              # 视图对象
│   │   ├── ProductVO.java              # 商品 VO
│   │   ├── CategoryVO.java             # 分类 VO
│   │   ├── BrandVO.java                # 品牌 VO
│   │   └── .gitkeep
│   └── NexusmallProductApplication.java # 启动类
└── src/main/resources/
    ├── mapper/                          # MyBatis XML
    │   ├── ProductMapper.xml
    │   ├── CategoryMapper.xml
    │   └── BrandMapper.xml
    └── application.yaml                 # 配置文件
```

## 💾 数据库设计

### 1. 商品表 (product)

```sql
CREATE TABLE `product` (
    `sku_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `sku_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
    `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类 ID',
    `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称',
    `brand_id` BIGINT(20) DEFAULT NULL COMMENT '品牌 ID',
    `brand_name` VARCHAR(100) DEFAULT NULL COMMENT '品牌名称',
    `description` TEXT COMMENT '商品描述',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`sku_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';
```

**设计要点：**
- 使用 `sku_id` 作为主键
- `version` 字段实现乐观锁，防止超卖
- 索引覆盖常用查询条件
- `status` 控制商品上下架

### 2. 商品分类表 (category)

```sql
CREATE TABLE `category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类 ID，0 为一级分类',
    `level` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级，2-二级，3-三级',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

**设计要点：**
- 支持多级分类（最多 3 级）
- `parent_id = 0` 表示一级分类
- `sort_order` 控制显示顺序

### 3. 商品品牌表 (brand)

```sql
CREATE TABLE `brand` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '品牌 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '品牌名称',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '品牌 Logo',
    `description` TEXT COMMENT '品牌描述',
    `first_letter` CHAR(1) DEFAULT NULL COMMENT '首字母',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_first_letter` (`first_letter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';
```

## 🔧 核心功能实现

### 1. 分布式事务

#### Service 层实现
```java
@Override
@GlobalTransactional(name = "decrease-stock-tx", rollbackFor = Exception.class)
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
```

#### Controller 层实现
```java
@PostMapping("/decreaseStock")
@GlobalTransactional(name = "decrease-stock-tx", rollbackFor = Exception.class)
public Result<Boolean> decreaseStock(
        @RequestParam("productId") Long productId,
        @RequestParam("count") Integer count) {
    boolean result = productService.decreaseStock(productId, count);
    return result ? Result.success(true, "库存扣减成功") : Result.fail("库存扣减失败");
}
```

### 2. 乐观锁机制

#### 数据库层面
```sql
UPDATE product
SET stock = stock - #{count}, version = version + 1
WHERE sku_id = #{skuId} AND stock >= #{count}
```

#### 应用层面
```java
int updateById(Product product);
// XML 中会检查 version
WHERE sku_id = #{skuId}
AND version = #{version}
```

### 3. Feign 远程调用

#### Feign 客户端定义
```java
@FeignClient(name = "nexusmall-product", fallback = ProductFeignFallback.class)
public interface ProductFeignService {
    @PostMapping("/product/decreaseStock")
    Result<Boolean> decreaseStock(@RequestParam("productId") Long productId, 
                                   @RequestParam("count") Integer count);
}
```

#### 降级处理
```java
@Slf4j
@Component
public class ProductFeignFallback implements ProductFeignService {
    @Override
    public Result<Boolean> decreaseStock(Long productId, Integer count) {
        log.error("扣减库存失败，商品 ID：{}，数量：{}", productId, count);
        return Result.fail("扣减库存失败");
    }
}
```

## 📡 API 接口文档

### 商品接口

#### 查询接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/product/ping` | GET | 健康检查 |
| `/product/list` | GET | 查询所有商品 |
| `/product/{skuId}` | GET | 根据 SKU ID 查询 |
| `/product/listByCondition` | GET | 条件查询 |
| `/product/checkStock` | GET | 检查库存 |

#### 操作接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/product/save` | POST | 新增商品 |
| `/product/update` | PUT | 更新商品 |
| `/product/delete/{skuId}` | DELETE | 删除商品 |
| `/product/putOnSale/{skuId}` | PUT | 上架商品 |
| `/product/putOffSale/{skuId}` | PUT | 下架商品 |

#### 库存接口（分布式事务）
| 接口 | 方法 | 描述 |
|------|------|------|
| `/product/decreaseStock` | POST | 扣减库存 |
| `/product/increaseStock` | POST | 增加库存 |
| `/product/batchDecreaseStock` | POST | 批量扣减 |
| `/product/batchIncreaseStock` | POST | 批量增加 |

### 分类接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/category/list` | GET | 查询所有分类 |
| `/category/{id}` | GET | 根据 ID 查询 |
| `/category/firstLevel` | GET | 查询一级分类 |
| `/category/listByParentId/{parentId}` | GET | 查询子分类 |
| `/category/save` | POST | 新增分类 |
| `/category/update` | PUT | 更新分类 |
| `/category/delete/{id}` | DELETE | 删除分类 |

### 品牌接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/brand/list` | GET | 查询所有品牌 |
| `/brand/{id}` | GET | 根据 ID 查询 |
| `/brand/getByName/{name}` | GET | 根据名称查询 |
| `/brand/save` | POST | 新增品牌 |
| `/brand/update` | PUT | 更新品牌 |
| `/brand/delete/{id}` | DELETE | 删除品牌 |

## 🔍 使用示例

### 示例 1：订单创建流程

```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private ProductFeignService productFeignService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    public void createOrder(Order order) {
        // 1. 扣减库存（跨服务调用）
        productFeignService.decreaseStock(order.getProductId(), order.getCount());
        
        // 2. 创建订单记录
        orderMapper.insert(order);
        
        // 3. 如果任何一步失败，整个事务回滚
    }
}
```

### 示例 2：批量扣减库存

```java
List<ProductStockDTO> stockDTOS = Arrays.asList(
    new ProductStockDTO(1L, 10),
    new ProductStockDTO(2L, 20),
    new ProductStockDTO(3L, 5)
);

productService.batchDecreaseStock(stockDTOS);
```

### 示例 3：条件查询商品

```bash
curl "http://localhost:10000/product/listByCondition?keyword=手机&categoryId=1&brandId=1&status=1"
```

## ⚙️ 配置说明

### application.yaml 关键配置

```yaml
# 数据源配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/nexusmall_product
    username: root
    password: your_password
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20

# Redis 配置
  redis:
    host: localhost
    port: 6379
    database: 0

# Nacos 配置
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: ${spring.cloud.nacos.discovery.server-addr}

# MyBatis 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.nexusmall.product.entity
  configuration:
    map-underscore-to-camel-case: true

# Seata 配置
seata:
  enabled: true
  application-id: nexusmall-product
  tx-service-group: nexusmall-tx-group
  service:
    vgroup-mapping:
      nexusmall-tx-group: default
    grouplist:
      default: 127.0.0.1:8091
```

## 🚀 部署指南

### 1. 环境准备
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+
- Seata Server 1.5.2
- JDK 8+
- Maven 3.6+

### 2. 数据库初始化
```bash
mysql -u root -p < product-table.sql
```

### 3. 启动 Seata Server
```bash
cd seata-1.5.2
bin\seata-server.bat
```

### 4. 编译打包
```bash
mvn clean package -DskipTests
```

### 5. 启动服务
```bash
java -jar target/nexusmall-product-0.0.1-SNAPSHOT.jar
```

## 🧪 测试建议

### 单元测试
- Service 层方法测试
- Mapper 层 SQL 测试
- Controller 层接口测试

### 集成测试
- 订单创建完整流程
- 分布式事务回滚测试
- Feign 调用测试

### 压力测试
- 高并发库存扣减
- 批量操作性能
- 数据库连接池性能

## 📈 性能优化建议

1. **缓存优化**
   - 热点商品缓存到 Redis
   - 分类数据缓存
   - 品牌数据缓存

2. **数据库优化**
   - 添加合适的索引
   - 分库分表（数据量大时）
   - 读写分离

3. **异步处理**
   - 库存变更异步通知
   - 消息队列解耦

4. **限流降级**
   - Sentinel 限流
   - Feign 降级
   - 数据库连接池限制

## 🔒 安全考虑

1. **权限控制**
   - 管理员才能修改商品信息
   - 库存操作需要特定权限

2. **数据验证**
   - 输入参数校验
   - 业务规则校验

3. **防重放攻击**
   - 库存操作添加请求 ID
   - 幂等性保证

## 📝 相关文档

- [PRODUCT-MODULE-README.md](./PRODUCT-MODULE-README.md) - 详细开发文档
- [PRODUCT-QUICKSTART.md](./PRODUCT-QUICKSTART.md) - 快速开始指南
- [PRODUCT-MODULE-SUMMARY.md](./PRODUCT-MODULE-SUMMARY.md) - 开发总结
- [seata-config.txt](./seata-config.txt) - Seata 配置说明
- [product-table.sql](./product-table.sql) - 数据库脚本

---

**版本**: 1.0.0  
**最后更新**: 2026-03-24  
**维护人员**: NexusMall Team
