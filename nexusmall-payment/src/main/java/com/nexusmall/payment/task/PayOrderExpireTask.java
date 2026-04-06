package com.nexusmall.payment.task;

import com.nexusmall.payment.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付单过期关闭定时任务
 * <p>
 * 每分钟执行一次，关闭超过30分钟未支付的订单
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayOrderExpireTask {

    private final PayOrderService payOrderService;

    /**
     * 每分钟执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeExpiredOrders() {
        log.info("【定时任务】开始执行关闭过期订单任务");

        try {
            int count = payOrderService.closeExpiredOrders();
            log.info("【定时任务】关闭过期订单完成，共关闭{}个订单", count);
        } catch (Exception e) {
            log.error("【定时任务】关闭过期订单异常", e);
        }
    }
}
