package com.nexusmall.promotion.interfaces.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 订单服务 Feign 客户端
 * <p>
 * 业界标准：微服务间通过Feign调用
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@FeignClient(name = "nexusmall-order", path = "/orders")
public interface OrderFeignClient {

    /**
     * 创建秒杀订单
     *
     * @param orderRequest 订单请求
     * @return 订单ID
     */
    @PostMapping("/seckill")
    Result<Long> createSeckillOrder(@RequestBody Map<String, Object> orderRequest);
}
