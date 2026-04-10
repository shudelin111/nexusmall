package com.nexusmall.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * <p>
 * 生产级实践：记录关键操作的审计日志，包括操作人、IP、耗时等
 * </p>
 * 
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * @DeleteMapping("/users/{id}")
 * @AuditLog(module = "用户管理", operation = "删除用户")
 * public Result<Void> deleteUser(@PathVariable Long id) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    /**
     * 模块名称
     *
     * @return 模块名
     */
    String module();
    
    /**
     * 操作描述
     *
     * @return 操作描述
     */
    String operation();
    
    /**
     * 是否记录请求参数（默认false，避免敏感信息泄露）
     *
     * @return 是否记录参数
     */
    boolean logParams() default false;
    
    /**
     * 是否记录响应结果（默认false，避免大数据量）
     *
     * @return 是否记录结果
     */
    boolean logResult() default false;
}
