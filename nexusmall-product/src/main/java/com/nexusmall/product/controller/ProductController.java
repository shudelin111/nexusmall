package com.nexusmall.product.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.constant.LogMessageConstants;
import com.nexusmall.common.constant.ResponseMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.util.RedisUtils;
import com.nexusmall.common.vo.Result;
import com.nexusmall.common.vo.UserBehaviorVO;
import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.service.ProductService;
import com.nexusmall.product.vo.ProductQueryRequest;
import com.nexusmall.product.vo.ProductVO;
import io.seata.core.context.RootContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器 - 处理所有商品相关请求
 */
@RestController
@RequestMapping("/")  // Gateway 已通过 /product/** 路由,StripPrefix 后直接访问
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
@Tag(name = "商品管理", description = "商品信息管理、库存管理")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 健康检查接口
     */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("service", "nexusmall-product");
        payload.put("status", "UP");
        payload.put("message", "product service is ready");

        // Verify redis connectivity
        String testKey = "ping:test";
        redisUtils.set(testKey, "ok");
        payload.put("redis", redisUtils.get(testKey));

        return Result.success(payload);
    }

    /**
     * 查询所有商品
     */
    @GetMapping(value = "/list", headers = "X-API-Version=v1")
    public Result<List<Product>> listProducts() {
        log.info("查询所有商品列表");
        List<Product> products = productService.listProducts();
        log.info("查询到{}条商品数据", products.size());
        return Result.success(products);
    }

    /**
     * 根据 SKU ID 查询商品（记录浏览行为）
     */
    @GetMapping(value = "/{skuId}", headers = "X-API-Version=v1")
    public Result<Product> getProduct(@PathVariable Long skuId,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String userName) {
        log.info("查询商品详情，skuId: {}, userId: {}", skuId, userId);
        Product product = productService.getBySkuId(skuId);
        
        // 发送用户浏览行为到 RocketMQ
        if (userId != null && product != null) {
            try {
                UserBehaviorVO behaviorVO = UserBehaviorVO.builder()
                    .userId(userId)
                    .behaviorType(UserBehaviorType.VIEW_PRODUCT.getCode())
                    .objectId(product.getSkuId())
                    .objectType("product")
                    .occurTime(LocalDateTime.now())
                    .build();
                
                Message<UserBehaviorVO> message = MessageBuilder.withPayload(behaviorVO).build();
                rocketMQTemplate.send("USER_BEHAVIOR_TOPIC", message);
                log.info("【发送浏览行为】userId: {}, productId: {}", userId, product.getSkuId());
            } catch (Exception e) {
                log.error("【发送浏览行为失败】userId: {}, productId: {}", userId, product.getSkuId(), e);
            }
        }
        
        return Result.success(product);
    }

    /**
     * 根据条件查询商品列表
     */
    @GetMapping(value = "/listByCondition", headers = "X-API-Version=v1")
    public Result<List<ProductVO>> listByCondition(@ModelAttribute ProductQueryRequest request) {
        log.info("条件查询商品，keyword: {}, categoryId: {}, brandId: {}, status: {}", 
                request.getKeyword(), request.getCategoryId(), request.getBrandId(), request.getStatus());
        List<ProductVO> products = productService.listByCondition(request);
        log.info("查询到{}条商品数据", products.size());
        
        // 发送用户搜索行为到 RocketMQ（仅当有搜索关键词且传了 userId 时）
        Long userId = request.getUserId();
        String keyword = request.getKeyword();
        if (userId != null && keyword != null && !keyword.trim().isEmpty()) {
            try {
                UserBehaviorVO behaviorVO = UserBehaviorVO.builder()
                    .userId(userId)
                    .behaviorType(UserBehaviorType.SEARCH_PRODUCT.getCode())
                    .objectId(null) // 搜索行为没有具体的对象 ID
                    .objectType("keyword")
                    .extraData("{\"keyword\":\"" + keyword + "\"}") // 将搜索词存入 extraData
                    .occurTime(LocalDateTime.now())
                    .build();
                
                Message<UserBehaviorVO> message = MessageBuilder.withPayload(behaviorVO).build();
                rocketMQTemplate.send("USER_BEHAVIOR_TOPIC", message);
                log.info("【发送搜索行为】userId: {}, keyword: {}", userId, keyword);
            } catch (Exception e) {
                log.error("【发送搜索行为失败】userId: {}, keyword: {}", userId, keyword, e);
            }
        }
        
        return Result.success(products);
    }

    /**
     * 新增商品
     */
    @PostMapping(value = "/save", headers = "X-API-Version=v1")
    public Result<Integer> saveProduct(@RequestBody ProductVO productVO) {
        log.info("新增商品，productName: {}, categoryId: {}", productVO.getSkuName(), productVO.getCategoryId());
        int result = productService.save(productVO);
        if (result > 0) {
            log.info(LogMessageConstants.Product.PRODUCT_ADDED, result);
            return Result.success(ResponseMessageConstants.Product.ADD_SUCCESS, result);
        } else {
            log.error("商品添加失败");
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Integer> updateProduct(@RequestBody ProductVO productVO) {
        log.info("更新商品，skuId: {}, productName: {}", productVO.getSkuId(), productVO.getSkuName());
        int result = productService.updateById(productVO);
        if (result > 0) {
            log.info(LogMessageConstants.Product.PRODUCT_UPDATED, result);
            return Result.success(ResponseMessageConstants.Product.UPDATE_SUCCESS, result);
        } else {
            log.error("商品更新失败，skuId: {}", productVO.getSkuId());
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{skuId}")
    public Result<Integer> deleteProduct(@PathVariable Long skuId) {
        log.info("删除商品，skuId: {}", skuId);
        int result = productService.deleteById(skuId);
        if (result > 0) {
            log.info(LogMessageConstants.Product.PRODUCT_DELETED, skuId);
            return Result.success(ResponseMessageConstants.Product.DELETE_SUCCESS, result);
        } else {
            log.error("商品删除失败，skuId: {}", skuId);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 扣减库存（支持分布式事务）
     */
    @PostMapping(value = "/decreaseStock", headers = "X-API-Version=v1")
    public Result<Boolean> decreaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        // 打印接收到的 XID，用于调试
        String xid = RootContext.getXID();
        log.info("====== Product 服务接收到 Feign 调用，XID: {}, productId: {}, count: {} ======", xid, productId, count);
        if (xid != null && !xid.isEmpty()) {
            log.info(LogMessageConstants.Product.XID_RECEIVED_SUCCESS, xid);
        } else {
            log.error(LogMessageConstants.Product.XID_NOT_RECEIVED);
        }
        boolean result = productService.decreaseStock(productId, count);
        return result ? Result.success(ResponseMessageConstants.Product.STOCK_DECREASE_SUCCESS, true) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 增加库存（支持分布式事务）
     */
    @PostMapping("/increaseStock")
    public Result<Boolean> increaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        log.info("增加库存，productId: {}, count: {}", productId, count);
        boolean result = productService.increaseStock(productId, count);
        if (result) {
            log.info(LogMessageConstants.Product.STOCK_INCREASED, productId, count);
            return Result.success(ResponseMessageConstants.Product.STOCK_INCREASE_SUCCESS, true);
        } else {
            log.error("库存增加失败，productId: {}, count: {}", productId, count);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 检查库存是否充足
     */
    @GetMapping("/checkStock")
    public Result<Boolean> checkStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        log.info("检查库存，productId: {}, count: {}", productId, count);
        boolean result = productService.checkStock(productId, count);
        log.info(result ? LogMessageConstants.Product.STOCK_CHECKED_SUFFICIENT : LogMessageConstants.Product.STOCK_CHECKED_INSUFFICIENT);
        return Result.success(result);
    }

    /**
     * 上架商品
     */
    @PutMapping("/putOnSale/{skuId}")
    public Result<Boolean> putOnSale(@PathVariable Long skuId) {
        log.info("商品上架，skuId: {}", skuId);
        boolean result = productService.putOnSale(skuId);
        if (result) {
            log.info(LogMessageConstants.Product.PRODUCT_PUT_ON_SALE, skuId);
            return Result.success(ResponseMessageConstants.Product.PUT_ON_SALE_SUCCESS, true);
        } else {
            log.error("商品上架失败，skuId: {}", skuId);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 下架商品
     */
    @PutMapping("/putOffSale/{skuId}")
    public Result<Boolean> putOffSale(@PathVariable Long skuId) {
        log.info("商品下架，skuId: {}", skuId);
        boolean result = productService.putOffSale(skuId);
        if (result) {
            log.info(LogMessageConstants.Product.PRODUCT_PUT_OFF_SALE, skuId);
            return Result.success(ResponseMessageConstants.Product.PUT_OFF_SALE_SUCCESS, true);
        } else {
            log.error("商品下架失败，skuId: {}", skuId);
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 批量扣减库存
     */
    @PostMapping("/batchDecreaseStock")
    public Result<Boolean> batchDecreaseStock(@RequestBody List<ProductStockDTO> stockDTOS) {
        log.info("批量扣减库存，stockDTOS size: {}", stockDTOS.size());
        boolean result = productService.batchDecreaseStock(stockDTOS);
        if (result) {
            log.info(LogMessageConstants.Product.BATCH_STOCK_DECREASED);
            return Result.success(ResponseMessageConstants.Product.BATCH_DECREASE_STOCK_SUCCESS, true);
        } else {
            log.error("批量扣减库存失败");
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }

    /**
     * 批量增加库存
     */
    @PostMapping("/batchIncreaseStock")
    public Result<Boolean> batchIncreaseStock(@RequestBody List<ProductStockDTO> stockDTOS) {
        log.info("批量增加库存，stockDTOS size: {}", stockDTOS.size());
        boolean result = productService.batchIncreaseStock(stockDTOS);
        if (result) {
            log.info(LogMessageConstants.Product.BATCH_STOCK_INCREASED);
            return Result.success(ResponseMessageConstants.Product.BATCH_INCREASE_STOCK_SUCCESS, true);
        } else {
            log.error("批量增加库存失败");
            return Result.failure(CommonResultCode.SYSTEM_ERROR);
        }
    }
}
