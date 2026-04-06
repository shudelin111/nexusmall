package com.nexusmall.member.config;

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
    public OpenAPI nexusmallMemberOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                // API 标题
                .title("NexusMall Member Service API")
                // API 描述
                .description("NexusMall 会员服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 会员档案管理：昵称、头像、生日、性别管理\n" +
                    "- 收货地址管理：增删改查收货地址\n" +
                    "- 会员等级管理：等级查询、升级通知\n" +
                    "- 积分管理：积分查询、兑换、消费记录\n" +
                    "- 成长值管理：成长值累计、等级升级\n\n" +
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
