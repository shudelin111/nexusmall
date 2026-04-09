package com.nexusmall.logistics.interfaces.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 通知服务 Feign 客户?
 * <p>
 * 业界标准：
 * - 物流状态变更时发送通知
 * - 支持短信、邮件、Push等多种通知方式
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@FeignClient(name = "nexusmall-notification", path = "/notifications")
public interface NotificationFeignClient {

    /**
     * 发送物流状态变更通知
     *
     * @param notification 通知内容
     * @return 是否成功
     */
    @PostMapping("/send")
    Result<Void> sendLogisticsNotification(@RequestBody Map<String, Object> notification);
}
