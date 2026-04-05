package com.nexusmall.order.feign;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author shudl
 * @create 2020-07-01 17:07
 * @description 商品服务 Feign 降级处理
 */
@Slf4j
@Component
public class ProductFeignFallback implements ProductFeignService {

    @Override
    public Result<Boolean> decreaseStock(Long productId, Integer count) {
        log.error("扣减库存失败，商品 ID: {}, 数量：{}", productId, count);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
