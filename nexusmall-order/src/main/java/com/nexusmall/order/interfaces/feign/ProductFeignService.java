package com.nexusmall.order.interfaces.feign;

import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户�?
 */
@FeignClient(name = "nexusmall-product", fallback = ProductFeignFallback.class)
public interface ProductFeignService {

    /**
     * 扣减库存
     */
    @PostMapping("/decreaseStock")  // 对应 ProductController: @RequestMapping("/") + @PostMapping("/decreaseStock")
    Result<Boolean> decreaseStock(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count
    );
}
