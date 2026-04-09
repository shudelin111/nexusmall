package com.nexusmall.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置�?(SpringDoc + Knife4j)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusmallSearchOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NexusMall Search Service API")
                .description("NexusMall 搜索服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 商品全文搜索：Elasticsearch 高性能搜索\n" +
                    "- 搜索历史：用户搜索记录管理\n" +
                    "- 热门搜索：热门关键词统计\n" +
                    "- 搜索建议：自动补全、联想词\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- Elasticsearch\n" +
                    "- MyBatis Plus\n" +
                    "- RocketMQ\n\n" +
                    "### 认证方式\n" +
                    "使用 JWT Token 进行身份验证，在请求头中添加 `Authorization: Bearer {token}`")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("NexusMall Team")
                    .email("support@nexusmall.com")
                    .url("https://github.com/nexusmall"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                .termsOfService("https://nexusmall.com/terms"));
    }
}
