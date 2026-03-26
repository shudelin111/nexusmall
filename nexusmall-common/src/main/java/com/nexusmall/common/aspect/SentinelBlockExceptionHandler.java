package com.nexusmall.common.aspect;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.enums.SentinelBlockType;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sentinel 全局流控异常处理器
 * <p>
 * 统一处理所有被 Sentinel 拦截的请求（限流、熔断、降级等）
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Slf4j
@Component
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        // 获取阻断类型（通过枚举匹配）
        SentinelBlockType blockType = SentinelBlockType.fromException(e);
        
        // 记录流控日志
        if (blockType != null) {
            log.warn("【Sentinel 流控】URI: {}, 类型：{}, 规则：{}", 
                    request.getRequestURI(), blockType.getName(), e.getRule());
        } else {
            log.warn("【Sentinel 流控】URI: {}, 类型：未知，规则：{}", 
                    request.getRequestURI(), e.getRule());
        }
        
        // 设置响应状态码和类型
        response.setStatus(getHttpStatus(blockType));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        
        // 根据阻断类型返回对应的响应
        Result<Void> result;
        if (blockType != null) {
            // 使用枚举中定义的错误码和消息
            result = Result.failure(
                blockType.getResultCode().getCode(), 
                blockType.getResultCode().getMessage()
            );
        } else {
            // 未知类型，使用默认错误码
            result = Result.failure(CommonResultCode.SENTINEL_UNKNOWN);
        }
        
        // 返回 JSON 响应
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 获取 HTTP 状态码
     *
     * @param blockType 阻断类型
     * @return HTTP 状态码
     */
    private int getHttpStatus(SentinelBlockType blockType) {
        if (blockType != null) {
            return blockType.getResultCode().getCode();
        }
        return CommonResultCode.SYSTEM_ERROR.getCode();
    }
}
