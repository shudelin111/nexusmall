package com.nexusmall.order.feign;

import com.nexusmall.common.vo.Result;
import com.nexusmall.order.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户端
 */
@FeignClient(name = "nexusmall-product", configuration = FeignConfig.class, fallback = ProductFeignFallback.class)
public interface ProductFeignService {

    /**
     * 扣减库存
     */
    @PostMapping("/product/decreaseStock")
    Result<Boolean> decreaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count
    );
}
