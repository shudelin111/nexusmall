package com.nexusmall.order.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Member 服务 Feign 客户端
 * <p>
 * 业界标准：
 * - Order 服务通过 Feign 调用 Member 服务
 * - 获取会员信息和收货地址
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@FeignClient(name = "nexusmall-member", fallback = MemberFeignFallback.class)
public interface MemberFeignClient {

    /**
     * 查询会员信息
     *
     * @param userId 用户 ID
     * @return 会员信息
     */
    @GetMapping("/info")  // 对应 MemberController: @RequestMapping("/") + @GetMapping("/info")
    Result<Map<String, Object>> getMemberInfo(@RequestParam("userId") Long userId);

    /**
     * 查询默认收货地址
     *
     * @param userId 用户 ID
     * @return 默认收货地址
     */
    @GetMapping("/address/default")  // 对应 MemberController: @RequestMapping("/") + @GetMapping("/address/default")
    Result<Map<String, Object>> getDefaultAddress(@RequestParam("userId") Long userId);
}
