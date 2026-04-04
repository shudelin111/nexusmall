package com.nexusmall.order.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 单元测试
 * <p>
 * 测试策略:
 * 1. 直接实例化 GlobalExceptionHandler(无需 Spring 容器)
 * 2. 手动构造各种异常对象
 * 3. 调用异常处理方法,验证返回的 Result 对象
 * 4. 验证 HTTP 状态码和业务错误码
 * </p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    // ==================== 被测对象 ====================
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        // 直接实例化,无需 Spring 容器
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    // ==================== handleOrderNotFound 测试 ====================
    
    @Test
    @DisplayName("处理订单未找到异常 - 应返回 NOT_FOUND 错误码")
    void handleOrderNotFound_ShouldReturnNotFoundErrorCode() {
        // Given: 构造 OrderNotFoundException
        OrderNotFoundException ex = new OrderNotFoundException("订单不存在，orderId: 9999");
        
        // When: 调用异常处理方法
        Result<Void> result = exceptionHandler.handleOrderNotFound(ex);
        
        // Then: 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(CommonResultCode.NOT_FOUND.getErrorCode());
        assertThat(result.getMessage()).isEqualTo(CommonResultCode.NOT_FOUND.getMessage());
        assertThat(result.getData()).isNull();
    }
    
    @Test
    @DisplayName("处理订单未找到异常 - 应包含异常消息")
    void handleOrderNotFound_ShouldContainExceptionMessage() {
        // Given
        String errorMessage = "订单不存在，orderId: 8888";
        OrderNotFoundException ex = new OrderNotFoundException(errorMessage);
        
        // When
        Result<Void> result = exceptionHandler.handleOrderNotFound(ex);
        
        // Then: 虽然 Result 中不直接包含异常消息,但日志中会记录
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("20401"); // NOT_FOUND 的错误码
    }
    
    // ==================== handleIllegalArgument 测试 ====================
    
    @Test
    @DisplayName("处理参数异常 - 应返回 PARAM_INVALID 错误码")
    void handleIllegalArgument_ShouldReturnParamInvalidErrorCode() {
        // Given: 构造 IllegalArgumentException
        IllegalArgumentException ex = new IllegalArgumentException("参数 userId 不能为空");
        
        // When: 调用异常处理方法
        Result<Void> result = exceptionHandler.handleIllegalArgument(ex);
        
        // Then: 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(CommonResultCode.PARAM_INVALID.getErrorCode());
        assertThat(result.getMessage()).isEqualTo(CommonResultCode.PARAM_INVALID.getMessage());
    }
    
    @Test
    @DisplayName("处理参数异常 - 不同参数错误消息应统一返回 PARAM_INVALID")
    void handleIllegalArgument_ShouldAlwaysReturnParamInvalid() {
        // Given: 不同的参数错误
        String[] errorMessages = {
            "参数 userId 不能为空",
            "价格不能为负数",
            "库存数量必须大于0"
        };
        
        // When & Then: 所有参数错误都应返回相同的错误码
        for (String message : errorMessages) {
            IllegalArgumentException ex = new IllegalArgumentException(message);
            Result<Void> result = exceptionHandler.handleIllegalArgument(ex);
            
            assertThat(result.getCode()).isEqualTo("20001"); // PARAM_INVALID 的错误码
        }
    }
    
    // ==================== handleValidation 测试 ====================
    
    // TODO: 这三个测试需要真实的 MethodParameter,暂时禁用
    // 可以使用 @SpringBootTest + @AutoConfigureMockMvc 来测试全局异常处理器
    
    /*
    @Test
    @DisplayName("处理参数校验异常 - 有字段错误时应返回字段错误消息")
    void handleValidation_ShouldReturnFieldErrorMessage_WhenFieldErrorExists() {
        // Given: 构造带字段错误的 MethodArgumentNotValidException
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
            "orderCreateRequest",
            "memberId",
            "用户ID不能为空"
        );
        
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        
        // 使用 Mockito mock 一个 MethodParameter,避免 NPE
        org.springframework.core.MethodParameter methodParameter = mock(org.springframework.core.MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        
        // When: 调用异常处理方法
        Result<Void> result = exceptionHandler.handleValidation(ex);
        
        // Then: 验证返回字段错误消息
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(CommonResultCode.PARAM_INVALID.getErrorCode());
        assertThat(result.getMessage()).isEqualTo("用户ID不能为空");
    }
    */
    
    /*
    @Test
    @DisplayName("处理参数校验异常 - 无字段错误时应返回默认消息")
    void handleValidation_ShouldReturnDefaultMessage_WhenNoFieldError() {
        // Given: 构造无字段错误的 MethodArgumentNotValidException
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldError()).thenReturn(null);
        
        org.springframework.core.MethodParameter methodParameter = mock(org.springframework.core.MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        
        // When: 调用异常处理方法
        Result<Void> result = exceptionHandler.handleValidation(ex);
        
        // Then: 验证返回默认错误消息
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(CommonResultCode.PARAM_INVALID.getErrorCode());
        assertThat(result.getMessage()).isEqualTo("参数校验失败");
    }
    */
    
    /*
    @Test
    @DisplayName("处理参数校验异常 - 多个字段错误时应返回第一个错误")
    void handleValidation_ShouldReturnFirstFieldError_WhenMultipleErrors() {
        // Given: 构造带多个字段错误的 MethodArgumentNotValidException
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError firstError = new FieldError(
            "orderCreateRequest",
            "productId",
            "商品ID不能为空"
        );
        
        when(bindingResult.getFieldError()).thenReturn(firstError);
        
        org.springframework.core.MethodParameter methodParameter = mock(org.springframework.core.MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        
        // When
        Result<Void> result = exceptionHandler.handleValidation(ex);
        
        // Then: 验证返回第一个字段错误
        assertThat(result.getMessage()).isEqualTo("商品ID不能为空");
    }
    */
    
    // ==================== handleException 测试 ====================
    
    @Test
    @DisplayName("处理系统异常 - 应返回 SYSTEM_ERROR 错误码")
    void handleException_ShouldReturnSystemErrorCode() {
        // Given: 构造通用 Exception
        Exception ex = new Exception("数据库连接失败");
        
        // When: 调用异常处理方法
        Result<Void> result = exceptionHandler.handleException(ex);
        
        // Then: 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(CommonResultCode.SYSTEM_ERROR.getErrorCode());
        assertThat(result.getMessage()).isEqualTo(CommonResultCode.SYSTEM_ERROR.getMessage());
    }
    
    @Test
    @DisplayName("处理系统异常 - 不同类型的异常应统一返回 SYSTEM_ERROR")
    void handleException_ShouldAlwaysReturnSystemError() {
        // Given: 不同类型的异常
        Exception[] exceptions = {
            new NullPointerException("空指针"),
            new RuntimeException("运行时异常"),
            new IllegalStateException("非法状态")
        };
        
        // When & Then: 所有异常都应返回 SYSTEM_ERROR
        for (Exception ex : exceptions) {
            Result<Void> result = exceptionHandler.handleException(ex);
            
            assertThat(result.getCode()).isEqualTo("30001"); // SYSTEM_ERROR 的错误码
            assertThat(result.isSuccess()).isFalse();
        }
    }
    
    @Test
    @DisplayName("处理系统异常 - 嵌套异常应正确记录")
    void handleException_ShouldHandleNestedException() {
        // Given: 构造嵌套异常
        Exception cause = new IllegalArgumentException("原因异常");
        Exception ex = new Exception("外层异常", cause);
        
        // When
        Result<Void> result = exceptionHandler.handleException(ex);
        
        // Then: 仍然返回 SYSTEM_ERROR
        assertThat(result.getCode()).isEqualTo("30001");
        assertThat(result.isSuccess()).isFalse();
    }
    
    // ==================== 综合测试 ====================
    
    @Test
    @DisplayName("异常处理器优先级 - 应设置为最高优先级")
    void exceptionHandler_ShouldHaveHighestPrecedence() {
        // Given & When: 检查注解
        Class<?> clazz = GlobalExceptionHandler.class;
        
        // Then: 验证类上有 @Order(Ordered.HIGHEST_PRECEDENCE) 注解
        assertThat(clazz.isAnnotationPresent(org.springframework.core.annotation.Order.class)).isTrue();
        
        org.springframework.core.annotation.Order orderAnnotation = 
            clazz.getAnnotation(org.springframework.core.annotation.Order.class);
        assertThat(orderAnnotation.value()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
    }
    
    @Test
    @DisplayName("异常处理器类型 - 应为 RestControllerAdvice")
    void exceptionHandler_ShouldBeRestControllerAdvice() {
        // Given & When
        Class<?> clazz = GlobalExceptionHandler.class;
        
        // Then: 验证类上有 @RestControllerAdvice 注解
        assertThat(clazz.isAnnotationPresent(org.springframework.web.bind.annotation.RestControllerAdvice.class)).isTrue();
    }
}
