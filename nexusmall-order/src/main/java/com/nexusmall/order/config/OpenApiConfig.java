package com.nexusmall.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置?(SpringDoc + Knife4j)
 * <p>
 * 业界标准配置,遵循 OpenAPI 3.0 规范
 * 官方文档: https://springdoc.org/
 * </p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 API 文档基本信息
     * 
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI nexusmallOrderOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                // API 标题
                .title("NexusMall Order Service API")
                // API 描述
                .description("NexusMall 订单服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 订单管理：创建、查询、更新、删除订单\n" +
                    "- 订单流程：支付、发货、确认收货、取消订单\n" +
                    "- 订单查询：按用户ID、状态、时间范围等条件查询\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- MyBatis Plus\n" +
                    "- RocketMQ\n" +
                    "- Seata 分布式事务\n\n" +
                    "### 认证方式\n" +
                    "使用 JWT Token 进行身份验证，在请求头中添加 `Authorization: Bearer {token}`")
                // API 版本
                .version("v1.0.0")
                // 联系人信?
                .contact(new Contact()
                    .name("NexusMall Team")
                    .email("support@nexusmall.com")
                    .url("https://github.com/nexusmall"))
                // 许可证信?
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                // 服务条款
                .termsOfService("https://nexusmall.com/terms"));
    }
}
