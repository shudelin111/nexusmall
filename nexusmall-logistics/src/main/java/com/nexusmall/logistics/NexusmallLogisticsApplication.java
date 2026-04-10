package com.nexusmall.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * NexusMall 物流服务启动类
 * <p>
 * 职责：
 * - 物流订单管理
 * - 物流轨迹跟踪（对接第三方物流API）
 * - 运费计算
 * - 仓库管理
 * - 发货/退货管理
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NexusmallLogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallLogisticsApplication.class, args);
    }
}
