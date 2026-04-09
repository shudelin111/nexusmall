package com.nexusmall.logistics.interfaces.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务 Feign 客户端
 * <p>
 * 业界标准：
 * - 用于查询订单详细信息
 * - 物流状态变更时通知订单服务
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@FeignClient(name = "nexusmall-order", path = "/orders")
public interface OrderFeignClient {

    /**
     * 根据订单号查询订单
     *
     * @param orderSn 订单编号
     * @return 订单信息
     */
    @GetMapping("/sn/{orderSn}")
    Result<Object> getOrderByOrderSn(@PathVariable("orderSn") String orderSn);

    /**
     * 更新订单物流状态
     *
     * @param orderId     订单ID
     * @param logisticsStatus 物流状态
     * @return 是否成功
     */
    @GetMapping("/{orderId}/logistics-status")
    Result<Void> updateLogisticsStatus(@PathVariable("orderId") Long orderId,
                                        @org.springframework.web.bind.annotation.RequestParam("status") Integer logisticsStatus);
}
