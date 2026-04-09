package com.nexusmall.common.annotation;

import com.nexusmall.common.util.DesensitizationUtils;

import java.lang.annotation.*;

/**
 * 敏感数据脱敏注解
 * <p>
 * 生产级实践：在VO/DTO字段上标注此注解，JSON序列化时自动脱敏
 * </p>
 * 
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * @Data
 * public class UserVO {
 *     
 *     @Sensitive(type = SensitiveType.PHONE)
 *     private String phone;  // 138****5678
 *     
 *     @Sensitive(type = SensitiveType.ID_CARD)
 *     private String idCard;  // 110101**********1234
 *     
 *     @Sensitive(type = SensitiveType.EMAIL)
 *     private String email;  // abc***@example.com
 * }
 * }</pre>
 *
 * @author shudl
 * @since 2026-04-09
 * @see DesensitizationUtils.SensitiveType
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {
    
    /**
     * 脱敏类型
     *
     * @return 脱敏策略
     */
    DesensitizationUtils.SensitiveType type();
}
