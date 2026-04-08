package com.nexusmall.logistics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类 (SpringDoc + Knife4j)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusmallLogisticsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NexusMall Logistics Service API")
                .description("NexusMall 物流服务 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- 物流订单管理：发货、运输、签收全流程\n" +
                    "- 物流轨迹跟踪：实时查询快递状态\n" +
                    "- 仓库管理：多仓库库存管理\n" +
                    "- 运费计算：按重量/体积/件数计费\n" +
                    "- 退货管理：退货申请、审核、收货\n\n" +
                    "### 技术栈\n" +
                    "- Spring Boot 2.7.18\n" +
                    "- Spring Cloud Alibaba\n" +
                    "- MyBatis Plus\n" +
                    "- RocketMQ\n" +
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
