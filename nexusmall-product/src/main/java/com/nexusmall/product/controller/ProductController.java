package com.nexusmall.product.controller;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.util.RedisUtils;
import com.nexusmall.common.vo.Result;
import com.nexusmall.common.vo.UserBehaviorVO;
import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.entity.Product;
import com.nexusmall.product.service.ProductService;
import com.nexusmall.product.vo.ProductVO;
import io.seata.core.context.RootContext;
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
@RequestMapping("/product")
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
    @GetMapping("/list")
    public Result<List<Product>> listProducts() {
        return Result.success(productService.listProducts());
    }

    /**
     * 根据 SKU ID 查询商品（记录浏览行为）
     */
    @GetMapping("/{skuId}")
    public Result<Product> getProduct(@PathVariable Long skuId,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String userName) {
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
    @GetMapping("/listByCondition")
    public Result<List<ProductVO>> listByCondition(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.listByCondition(keyword, categoryId, brandId, status));
    }

    /**
     * 新增商品
     */
    @PostMapping("/save")
    public Result<Integer> saveProduct(@RequestBody ProductVO productVO) {
        int result = productService.save(productVO);
        return result > 0 ? Result.success("商品添加成功", result) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "商品添加失败");
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Integer> updateProduct(@RequestBody ProductVO productVO) {
        int result = productService.updateById(productVO);
        return result > 0 ? Result.success("商品更新成功", result) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "商品更新失败");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{skuId}")
    public Result<Integer> deleteProduct(@PathVariable Long skuId) {
        int result = productService.deleteById(skuId);
        return result > 0 ? Result.success("商品删除成功", result) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "商品删除失败");
    }

    /**
     * 扣减库存（支持分布式事务）
     */
    @PostMapping("/decreaseStock")
    public Result<Boolean> decreaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        // 打印接收到的 XID，用于调试
        String xid = RootContext.getXID();
        log.info("====== Product 服务接收到 Feign 调用，XID: {}, productId: {}, count: {} ======", xid, productId, count);
        if (xid != null && !xid.isEmpty()) {
            log.info("✓ Product 服务成功接收到 XID: {}", xid);
        } else {
            log.error("✗ Product 服务未接收到 XID！");
        }
        boolean result = productService.decreaseStock(productId, count);
        return result ? Result.success("库存扣减成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "库存扣减失败");
    }

    /**
     * 增加库存（支持分布式事务）
     */
    @PostMapping("/increaseStock")
    public Result<Boolean> increaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        boolean result = productService.increaseStock(productId, count);
        return result ? Result.success("库存增加成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "库存增加失败");
    }

    /**
     * 检查库存是否充足
     */
    @GetMapping("/checkStock")
    public Result<Boolean> checkStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        return Result.success(productService.checkStock(productId, count));
    }

    /**
     * 上架商品
     */
    @PutMapping("/putOnSale/{skuId}")
    public Result<Boolean> putOnSale(@PathVariable Long skuId) {
        boolean result = productService.putOnSale(skuId);
        return result ? Result.success("商品上架成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "商品上架失败");
    }

    /**
     * 下架商品
     */
    @PutMapping("/putOffSale/{skuId}")
    public Result<Boolean> putOffSale(@PathVariable Long skuId) {
        boolean result = productService.putOffSale(skuId);
        return result ? Result.success("商品下架成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "商品下架失败");
    }

    /**
     * 批量扣减库存
     */
    @PostMapping("/batchDecreaseStock")
    public Result<Boolean> batchDecreaseStock(@RequestBody List<ProductStockDTO> stockDTOS) {
        boolean result = productService.batchDecreaseStock(stockDTOS);
        return result ? Result.success("批量扣减库存成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "批量扣减库存失败");
    }

    /**
     * 批量增加库存
     */
    @PostMapping("/batchIncreaseStock")
    public Result<Boolean> batchIncreaseStock(@RequestBody List<ProductStockDTO> stockDTOS) {
        boolean result = productService.batchIncreaseStock(stockDTOS);
        return result ? Result.success("批量增加库存成功", true) : Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "批量增加库存失败");
    }
}
