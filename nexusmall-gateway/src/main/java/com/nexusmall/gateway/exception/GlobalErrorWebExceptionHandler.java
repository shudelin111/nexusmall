package com.nexusmall.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.GatewayException;
import com.nexusmall.common.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway 全局异常处理器
 * 统一处理 WebFlux 应用中的异常，返回标准化的 Result 响应
 */
@Component
@Order(-1) // 确保优先级最高
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 根据异常类型返回对应的 Result 响应
        Result<?> result;
        HttpStatus httpStatus;
        
        if (ex instanceof RuntimeException && ex.getMessage() != null 
                && ex.getMessage().contains("Token 验证失败")) {
            // Token 验证失败 - 返回 401
            httpStatus = HttpStatus.UNAUTHORIZED;
            result = Result.failure(CommonResultCode.UNAUTHORIZED);
            log.warn("Token 验证失败：path={}, method={}, error={}", 
                    exchange.getRequest().getPath(), 
                    exchange.getRequest().getMethod(),
                    ex.getMessage());
        } else if (ex instanceof ResponseStatusException) {
            // 业务异常 - 返回对应的状态码
            ResponseStatusException rse = (ResponseStatusException) ex;
            httpStatus = HttpStatus.valueOf(rse.getRawStatusCode());
            String message = rse.getReason() != null ? rse.getReason() : "请求失败";
            result = Result.failure(httpStatus.value(), message);
            log.error("ResponseStatusException: status={}, reason={}", httpStatus, rse.getReason());
        } else {
            // 系统异常 - 返回 500
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.failure(CommonResultCode.SYSTEM_ERROR);
            log.error("Gateway 处理请求异常：path={}, method={}, error={}", 
                    exchange.getRequest().getPath(),
                    exchange.getRequest().getMethod(),
                    ex.getMessage(), ex);
        }
        
        // 设置响应状态码
        response.setStatusCode(httpStatus);
        
        // 将 Result 对象序列化为 JSON 并写入响应
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // ObjectMapper 序列化失败属于系统异常，记录日志后抛出网关业务异常
            log.error("JSON 序列化失败，result={}", result, e);
            throw new GatewayException(CommonResultCode.JSON_SERIALIZE_FAILED.getCode(), CommonResultCode.JSON_SERIALIZE_FAILED.getMessage(), e);
        }
        
        DataBufferFactory bufferFactory = response.bufferFactory();
        final String finalJsonResponse = jsonResponse; // lambda 表达式要求变量为 final
        return response.writeWith(Mono.fromSupplier(() -> 
                bufferFactory.wrap(finalJsonResponse.getBytes(StandardCharsets.UTF_8))));
    }
}
