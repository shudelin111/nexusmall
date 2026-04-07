package com.nexusmall.order.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.interfaces.dto.OrderCreateRequest;
import com.nexusmall.order.interfaces.exception.GlobalExceptionHandler;
import com.nexusmall.order.interfaces.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleOrderNotFound() {
        Result<Void> result = handler.handleOrderNotFound(new OrderNotFoundException("missing"));
        assertThat(result.getCode()).isEqualTo(CommonResultCode.NOT_FOUND.getErrorCode());
    }

    @Test
    void shouldHandleIllegalArgument() {
        Result<Void> result = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertThat(result.getCode()).isEqualTo(CommonResultCode.PARAM_INVALID.getErrorCode());
    }

    @Test
    void shouldHandleValidation() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldError()).thenReturn(new FieldError("req", "memberId", "required"));
        MethodParameter methodParameter = validationMethodParameter();

        Result<Void> result = handler.handleValidation(new MethodArgumentNotValidException(methodParameter, bindingResult));
        assertThat(result.getCode()).isEqualTo(CommonResultCode.PARAM_INVALID.getErrorCode());
    }

    @Test
    void shouldHandleSystemException() {
        Result<Void> result = handler.handleException(new RuntimeException("boom"));
        assertThat(result.getCode()).isEqualTo(CommonResultCode.SYSTEM_ERROR.getErrorCode());
    }

    @Test
    void shouldKeepAnnotations() {
        assertThat(GlobalExceptionHandler.class.isAnnotationPresent(RestControllerAdvice.class)).isTrue();
        assertThat(GlobalExceptionHandler.class.getAnnotation(org.springframework.core.annotation.Order.class).value())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    private MethodParameter validationMethodParameter() {
        try {
            return new MethodParameter(
                    GlobalExceptionHandlerTest.class.getDeclaredMethod("validationTarget", OrderCreateRequest.class),
                    0
            );
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unused")
    private void validationTarget(OrderCreateRequest request) {
    }
}
