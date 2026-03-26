package com.nexusmall.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.GatewayException;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway 全局异常处理器
 * <p>
 * 统一处理 WebFlux 应用中的异常，返回标准化的 Result 响应
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    
    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置响应头为 JSON 格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 根据异常类型返回对应的 Result 响应
        Result<?> result;
        HttpStatus httpStatus;
        
        // 1. JWT Token 相关异常 - 返回 401
        if (isJwtException(ex)) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            result = Result.failure(CommonResultCode.UNAUTHORIZED);
            log.warn("JWT Token 验证失败：path={}, method={}, error={}", 
                    exchange.getRequest().getPath(), 
                    exchange.getRequest().getMethod(),
                    ex.getMessage());
        }
        // 2. Gateway 业务异常 - 返回对应状态码
        else if (ex instanceof GatewayException) {
            GatewayException ge = (GatewayException) ex;
            httpStatus = resolveHttpStatus(ge);
            result = buildResultFromGatewayException(httpStatus, ge);
            log.error("网关业务异常：status={}, path={}, method={}, reason={}", 
                    httpStatus,
                    exchange.getRequest().getPath(),
                    exchange.getRequest().getMethod(),
                    ge.getMessage());
        }
        // 3. 其他系统异常 - 返回 500
        else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.failure(CommonResultCode.SYSTEM_ERROR);
            log.error("系统异常：path={}, method={}, error={}", 
                    exchange.getRequest().getPath(),
                    exchange.getRequest().getMethod(),
                    ex.getMessage(), ex);
        }
        
        // 设置响应状态码
        response.setStatusCode(httpStatus);
        
        // 将 Result 对象序列化为 JSON 并写入响应
        return writeResponse(response, result);
    }
    
    /**
     * 判断是否为 JWT Token 验证失败的异常
     * <p>
     * JWT Filter 中已将所有 JWT 相关异常转换为 GatewayException(错误码为 UNAUTHORIZED)
     * </p>
     *
     * @param ex 异常对象
     * @return true-是 JWT 验证失败异常，false-不是
     */
    private boolean isJwtException(Throwable ex) {
        // JWT Filter 会将所有 JWT 异常包装成 GatewayException，错误码为 UNAUTHORIZED
        if (!(ex instanceof GatewayException)) {
            return false;
        }
        GatewayException ge = (GatewayException) ex;
        return CommonResultCode.UNAUTHORIZED.getErrorCode().equals(ge.getCode());
    }

    /**
     * 解析 GatewayException 的 HTTP 状态码
     * <p>
     * 优先使用异常中的状态码，如果无效则降级为 500
     * </p>
     *
     * @param ge GatewayException 异常
     * @return HTTP 状态码，无效时返回 INTERNAL_SERVER_ERROR
     */
    private HttpStatus resolveHttpStatus(GatewayException ge) {
        try {
            // 根据错误码推断 HTTP 状态码
            String code = ge.getCode();
            if (code == null) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
            
            if (CommonResultCode.UNAUTHORIZED.getErrorCode().equals(code)) {
                return HttpStatus.UNAUTHORIZED;
            } else if (CommonResultCode.FORBIDDEN.getErrorCode().equals(code)) {
                return HttpStatus.FORBIDDEN;
            } else if (CommonResultCode.NOT_FOUND.getErrorCode().equals(code)) {
                return HttpStatus.NOT_FOUND;
            } else if (CommonResultCode.PARAM_INVALID.getErrorCode().equals(code)) {
                return HttpStatus.BAD_REQUEST;
            } else {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } catch (Exception e) {
            log.warn("解析 GatewayException 状态码失败：{}, 默认返回 500", ge.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * 从 GatewayException 构建 Result 对象
     * <p>
     * 遵循业界标准：HTTP 状态码用于协议层，业务错误码用于响应体
     * </p>
     *
     * @param httpStatus HTTP 状态码
     * @param ge GatewayException 异常
     * @return Result 响应对象
     */
    private Result<?> buildResultFromGatewayException(HttpStatus httpStatus, GatewayException ge) {
        // 业界最佳实践：响应体中使用业务错误码，HTTP 状态码用于协议层
        String message = ge.getMessage() != null ? ge.getMessage() : "请求失败";
        
        if (httpStatus == HttpStatus.BAD_REQUEST) {
            return Result.failure(CommonResultCode.PARAM_INVALID);
        } else if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return Result.failure(CommonResultCode.UNAUTHORIZED);
        } else if (httpStatus == HttpStatus.FORBIDDEN) {
            return Result.failure(CommonResultCode.FORBIDDEN);
        } else if (httpStatus == HttpStatus.NOT_FOUND) {
            return Result.failure(CommonResultCode.NOT_FOUND);
        } else {
            return Result.failure(String.valueOf(httpStatus.value()), message);
        }
    }
    
    /**
     * 将 Result 对象写入 HTTP 响应
     * <p>
     * 使用 UTF-8 编码序列化 JSON 并写入响应流
     * </p>
     * 
     * @param response HTTP 响应对象
     * @param result 业务结果对象
     * @return 写入完成的 Mono
     */
    private Mono<Void> writeResponse(ServerHttpResponse response, Result<?> result) {
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // ObjectMapper 序列化失败属于系统异常，记录日志后抛出网关业务异常
            log.error("JSON 序列化失败，result={}", result, e);
            throw new GatewayException(CommonResultCode.JSON_SERIALIZE_FAILED.getErrorCode(), 
                    CommonResultCode.JSON_SERIALIZE_FAILED.getMessage(), e);
        }
        
        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.fromSupplier(() -> 
                bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8))));
    }
}
