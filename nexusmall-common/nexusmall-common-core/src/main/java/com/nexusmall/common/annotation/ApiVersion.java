package com.nexusmall.common.annotation;

import java.lang.annotation.*;

/**
 * API 版本注解
 * <p>
 * 用于标记 Controller 或方法支持的 API 版本
 * </p>
 *
 * <pre>
 * 使用示例：
 * {@code
 * @ApiVersion("v1")
 * @RestController
 * public class OrderController {
 *     
 *     @ApiVersion("v2")  // 方法级别覆盖类级别
 *     @PostMapping("/create")
 *     public Result<Order> createOrder() {
 *         // ...
 *     }
 * }
 * }
 * </pre>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * API 版本号
     *
     * @return 版本号（如 "v1", "v2"）
     */
    String value();
}
