package com.nexusmall.common.aspect;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

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
        // 记录流控日志
        log.warn("【Sentinel 流控】URI: {}, 类型：{}, 规则：{}", 
                request.getRequestURI(), getExceptionType(e), e.getRule());
        
        // 设置响应状态码和类型
        response.setStatus(getHttpStatus(e));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        
        // 根据异常类型返回不同的响应
        Result<Void> result;
        switch (getExceptionType(e)) {
            case "流控":
                result = Result.failure(CommonResultCode.SYSTEM_BUSY.getCode(), "访问过于频繁，请稍后再试");
                break;
            case "熔断降级":
                result = Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "服务暂时不可用，请稍后重试");
                break;
            case "热点参数限流":
                result = Result.failure(CommonResultCode.SYSTEM_BUSY.getCode(), "访问过于频繁，请调整访问参数");
                break;
            case "系统保护":
                result = Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后再试");
                break;
            case "授权异常":
                result = Result.failure(CommonResultCode.FORBIDDEN.getCode(), "无权限访问该资源");
                break;
            default:
                result = Result.failure(CommonResultCode.SYSTEM_ERROR.getCode(), "请求被拦截");
        }
        
        // 返回 JSON 响应
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 获取异常类型描述
     *
     * @param e BlockException
     * @return 异常类型
     */
    private String getExceptionType(BlockException e) {
        if (e instanceof FlowException) {
            return "流控";
        } else if (e instanceof DegradeException) {
            return "熔断降级";
        } else if (e instanceof ParamFlowException) {
            return "热点参数限流";
        } else if (e instanceof SystemBlockException) {
            return "系统保护";
        } else if (e instanceof AuthorityException) {
            return "授权异常";
        } else {
            return "未知类型";
        }
    }

    /**
     * 获取 HTTP 状态码
     *
     * @param e BlockException
     * @return HTTP 状态码
     */
    private int getHttpStatus(BlockException e) {
        if (e instanceof FlowException || e instanceof ParamFlowException) {
            return 429; // Too Many Requests
        } else if (e instanceof DegradeException || e instanceof SystemBlockException) {
            return 503; // Service Unavailable
        } else if (e instanceof AuthorityException) {
            return 403; // Forbidden
        } else {
            return 500; // Internal Server Error
        }
    }
}
