package com.nexusmall.product.feign;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 商品服务 Feign 降级处理
 */
@Slf4j
@Component
public class ProductFeignFallback implements ProductFeignService {

    /**
     * 扣减库存失败降级
     */
    @Override
    public Result<Boolean> decreaseStock(Long productId, Integer count) {
        log.error("扣减库存失败，商品 ID: {}, 数量：{}", productId, count);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 增加库存失败降级
     */
    @Override
    public Result<Boolean> increaseStock(Long productId, Integer count) {
        log.error("增加库存失败，商品 ID: {}, 数量：{}", productId, count);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 检查库存失败降级
     */
    @Override
    public Result<Boolean> checkStock(Long productId, Integer count) {
        log.error("检查库存失败，商品 ID: {}, 数量：{}", productId, count);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
