package com.nexusmall.order.feign;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Member 服务 Feign 降级处理
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
public class MemberFeignFallback implements MemberFeignClient {

    @Override
    public Result<Map<String, Object>> getMemberInfo(Long userId) {
        log.error("获取会员信息失败，userId: {}", userId);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    @Override
    public Result<Map<String, Object>> getDefaultAddress(Long userId) {
        log.error("获取默认收货地址失败，userId: {}", userId);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
