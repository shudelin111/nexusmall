package com.nexusmall.promotion.config;

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
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusmallPromotionOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NexusMall Promotion Service API")
                .description("NexusMall 营销服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 优惠券管理：创建、查询、领取、使用优惠券\n" +
                    "- 秒杀活动：限时抢购、库存控制、防超卖\n" +
                    "- 满减活动：阶梯优惠、自动计算\n" +
                    "- 促销活动：组合营销、规则引擎\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- MyBatis Plus\n" +
                    "- Redisson 分布式锁\n" +
                    "- Seata 分布式事务\n\n" +
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
