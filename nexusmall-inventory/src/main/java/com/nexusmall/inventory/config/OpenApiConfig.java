package com.nexusmall.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类(SpringDoc + Knife4j)
 * <p>
 * 业界标准配置,遵循 OpenAPI 3.0 规范
 * 官方文档: https://springdoc.org/
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 API 文档基本信息
     * 
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI nexusmallInventoryOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                // API 标题
                .title("NexusMall Inventory Service API")
                // API 描述
                .description("NexusMall 库存服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- SKU库存管理：库存查询、扣减、回滚、确认\n" +
                    "- 库存流水记录：所有库存变动操作追踪与审计\n" +
                    "- 库存预警机制：低库存提醒与阈值设置\n" +
                    "- 分布式锁防超卖：Redisson分布式锁保障并发安全\n" +
                    "- 乐观锁并发控制：数据库版本号防止数据冲突\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- MyBatis Plus\n" +
                    "- Redisson 分布式锁\n" +
                    "- Seata 分布式事务\n\n" +
                    "### 认证方式\n" +
                    "使用 JWT Token 进行身份验证，在请求头中添加 `Authorization: Bearer {token}`")
                // API 版本
                .version("v1.0.0")
                // 联系人信息
                .contact(new Contact()
                    .name("NexusMall Team")
                    .email("support@nexusmall.com")
                    .url("https://github.com/nexusmall"))
                // 许可证信息
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                // 服务条款
                .termsOfService("https://nexusmall.com/terms"));
    }
}
