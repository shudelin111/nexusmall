package com.nexusmall.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类 (SpringDoc + Knife4j)
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
    public OpenAPI nexusmallCartOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                // API 标题
                .title("NexusMall Cart Service API")
                // API 描述
                .description("NexusMall 购物车服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 购物车管理：添加商品、删除商品、修改数量、查询列表\n" +
                    "- Redis Hash 主存储：高性能读写操作\n" +
                    "- MySQL 异步持久化：数据兜底保障\n" +
                    "- 商品快照机制：防止商家改价导致体验问题\n" +
                    "- 匿名购物车合并：未登录→登录后自动合并\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- MyBatis Plus\n" +
                    "- Redis Hash 数据结构\n" +
                    "- RocketMQ 异步消息\n\n" +
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
