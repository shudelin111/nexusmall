package com.nexusmall.product.interfaces.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户端（供其他微服务调用户
 */
@FeignClient(name = "nexusmall-product", fallback = ProductFeignFallback.class)
public interface ProductFeignService {

    /**
     * 扣减库存
     * @param productId 商品 ID
     * @param count 数量
     * @return 操作结果
     */
    @PostMapping("/decreaseStock")
    Result<Boolean> decreaseStock(@RequestParam("productId") Long productId, 
                                   @RequestParam("count") Integer count);

    /**
     * 增加库存
     * @param productId 商品 ID
     * @param count 数量
     * @return 操作结果
     */
    @PostMapping("/increaseStock")
    Result<Boolean> increaseStock(@RequestParam("productId") Long productId, 
                                   @RequestParam("count") Integer count);

    /**
     * 检查库存是否充足
     * @param productId 商品 ID
     * @param count 数量
     * @return 库存是否充足
     */
    @GetMapping("/checkStock")
    Result<Boolean> checkStock(@RequestParam("productId") Long productId,
                                @RequestParam("count") Integer count);
}
