# Product 模块开发完成说明

## 一、数据库表结构

### 1. 商品表 (product)
- `sku_id`: 主键，自增
- `sku_name`: 商品名称
- `price`: 商品价格
- `stock`: 库存数量
- `category_id`: 分类 ID
- `category_name`: 分类名称
- `brand_id`: 品牌 ID
- `brand_name`: 品牌名称
- `description`: 商品描述
- `status`: 状态（0-下架，1-上架）
- `create_time`: 创建时间
- `update_time`: 更新时间
- `version`: 版本号（乐观锁）

### 2. 商品分类表 (category)
- `id`: 主键，自增
- `name`: 分类名称
- `parent_id`: 父分类 ID（0 为一级分类）
- `level`: 分类层级（1-一级，2-二级，3-三级）
- `sort_order`: 排序
- `icon`: 分类图标
- `status`: 状态（0-禁用，1-启用）
- `create_time`: 创建时间
- `update_time`: 更新时间

### 3. 商品品牌表 (brand)
- `id`: 主键，自增
- `name`: 品牌名称
- `logo`: 品牌 Logo
- `description`: 品牌描述
- `first_letter`: 首字母
- `sort_order`: 排序
- `status`: 状态（0-禁用，1-启用）
- `create_time`: 创建时间
- `update_time`: 更新时间

## 二、项目结构

```
nexusmall-product/
├── src/main/java/com/nexusmall/product/
│   ├── config/
│   │   └── FeignConfig.java                 # Feign 配置类
│   ├── controller/
│   │   ├── ProductController.java           # 商品控制器
│   │   ├── CategoryController.java          # 分类控制器
│   │   └── BrandController.java             # 品牌控制器
│   ├── dao/
│   │   ├── ProductMapper.java               # 商品 Mapper 接口
│   │   ├── CategoryMapper.java              # 分类 Mapper 接口
│   │   ├── BrandMapper.java                 # 品牌 Mapper 接口
│   │   └── ProductStockDTO.java             # 商品库存 DTO
│   ├── entity/
│   │   ├── Product.java                     # 商品实体
│   │   ├── Category.java                    # 分类实体
│   │   └── Brand.java                       # 品牌实体
│   ├── exception/
│   │   ├── ProductNotFoundException.java    # 商品未找到异常
│   │   └── GlobalExceptionHandler.java      # 全局异常处理
│   ├── feign/
│   │   ├── ProductFeignService.java         # Feign 客户端接口
│   │   └── ProductFeignFallback.java        # Feign 降级处理
│   ├── service/
│   │   ├── ProductService.java              # 商品服务接口
│   │   ├── CategoryService.java             # 分类服务接口
│   │   ├── BrandService.java                # 品牌服务接口
│   │   └── impl/
│   │       ├── ProductServiceImpl.java      # 商品服务实现
│   │       ├── CategoryServiceImpl.java     # 分类服务实现
│   │       └── BrandServiceImpl.java        # 品牌服务实现
│   ├── vo/
│   │   ├── ProductVO.java                   # 商品 VO
│   │   ├── CategoryVO.java                  # 分类 VO
│   │   └── BrandVO.java                     # 品牌 VO
│   └── NexusmallProductApplication.java     # 启动类
└── src/main/resources/
    ├── mapper/
    │   ├── ProductMapper.xml                # 商品 Mapper XML
    │   ├── CategoryMapper.xml               # 分类 Mapper XML
    │   └── BrandMapper.xml                  # 品牌 Mapper XML
    └── application.yaml                     # 配置文件
```

## 三、API 接口列表

### 1. 商品接口 (/product)

#### 查询接口
- `GET /product/ping` - 健康检查
- `GET /product/list` - 查询所有商品
- `GET /product/{skuId}` - 根据 SKU ID 查询商品
- `GET /product/listByCondition` - 根据条件查询商品列表
- `GET /product/checkStock` - 检查库存是否充足

#### 操作接口
- `POST /product/save` - 新增商品
- `PUT /product/update` - 更新商品
- `DELETE /product/delete/{skuId}` - 删除商品
- `PUT /product/putOnSale/{skuId}` - 上架商品
- `PUT /product/putOffSale/{skuId}` - 下架商品

#### 库存接口（支持分布式事务）
- `POST /product/decreaseStock` - 扣减库存
- `POST /product/increaseStock` - 增加库存
- `POST /product/batchDecreaseStock` - 批量扣减库存
- `POST /product/batchIncreaseStock` - 批量增加库存

### 2. 分类接口 (/category)

- `GET /category/list` - 查询所有分类
- `GET /category/{id}` - 根据 ID 查询分类
- `GET /category/firstLevel` - 查询一级分类
- `GET /category/listByParentId/{parentId}` - 根据父 ID 查询子分类
- `GET /category/listByLevel/{level}` - 根据层级查询分类
- `GET /category/listByStatus/{status}` - 根据状态查询分类
- `POST /category/save` - 新增分类
- `PUT /category/update` - 更新分类
- `DELETE /category/delete/{id}` - 删除分类

### 3. 品牌接口 (/brand)

- `GET /brand/list` - 查询所有品牌
- `GET /brand/{id}` - 根据 ID 查询品牌
- `GET /brand/getByName/{name}` - 根据名称查询品牌
- `GET /brand/listByStatus/{status}` - 根据状态查询品牌
- `GET /brand/listByFirstLetter/{firstLetter}` - 根据首字母查询品牌
- `POST /brand/save` - 新增品牌
- `PUT /brand/update` - 更新品牌
- `DELETE /brand/delete/{id}` - 删除品牌

## 四、核心功能说明

### 1. 分布式事务支持
- 所有库存操作接口都添加了 `@GlobalTransactional` 注解
- 支持 Seata AT 模式
- 库存扣减失败会自动回滚

### 2. 乐观锁机制
- 商品表包含 `version` 字段
- 更新操作会检查版本号，防止并发问题

### 3. Feign 远程调用
- 提供了 `ProductFeignService` 供其他微服务调用
- 实现了降级处理 `ProductFeignFallback`
- 支持服务熔断和降级

### 4. 缓存支持
- 集成了 Redis 缓存
- 可通过 `/product/redis/debug/{skuId}` 测试缓存功能

## 五、使用示例

### 1. 创建订单时扣减库存（跨服务分布式事务）

```java
// OrderService 中调用
@GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
public void createOrder(Order order) {
    // 1. 扣减库存（通过 Feign 调用 product 服务）
    productFeignService.decreaseStock(order.getProductId(), order.getCount());
    
    // 2. 创建订单记录
    orderMapper.insert(order);
}
```

### 2. 批量扣减库存

```java
List<ProductStockDTO> stockDTOS = new ArrayList<>();
stockDTOS.add(new ProductStockDTO(1L, 10));
stockDTOS.add(new ProductStockDTO(2L, 20));

productFeignService.batchDecreaseStock(stockDTOS);
```

### 3. 查询商品列表

```bash
# 查询所有商品
curl http://localhost:8082/product/list

# 按条件查询
curl http://localhost:8082/product/listByCondition?keyword=手机&categoryId=1&status=1
```

## 六、注意事项

### 1. 数据库初始化
执行 `product-table.sql` 创建表和初始数据

### 2. Seata 配置
确保已配置 Seata Server，地址为 `127.0.0.1:8091`

### 3. 依赖管理
已在 `pom.xml` 中添加以下关键依赖：
- spring-cloud-starter-alibaba-seata（分布式事务）
- nexusmall-common（公共模块）
- spring-cloud-starter-openfeign（服务调用）

### 4. 事务回滚
- 库存不足时会抛出 RuntimeException
- Seata 会自动回滚所有分支事务
- 确保 Feign 客户端配置了 fallback

## 七、测试建议

1. **单元测试**：测试各个 Service 方法
2. **集成测试**：测试完整的订单创建流程
3. **压力测试**：测试高并发下的库存扣减
4. **分布式事务测试**：模拟失败场景，验证回滚机制

## 八、后续优化建议

1. 添加商品搜索功能（可集成 Elasticsearch）
2. 添加商品评论功能
3. 添加商品收藏功能
4. 优化库存扣减性能（可使用 Redis 预扣减）
5. 添加商品审核流程
6. 添加商品图片上传功能
