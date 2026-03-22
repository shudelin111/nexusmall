package com.nexusmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()  // 允许监控端点公开访问
                .pathMatchers("/product/**").authenticated()  // 产品服务需要认证
                .anyExchange().permitAll()  // 其他请求允许访问
            )
            .csrf(csrf -> csrf.disable())  // 网关通常不需要CSRF保护
            .formLogin(formLogin -> formLogin.disable())  // 禁用表单登录，使用JWT
            .httpBasic(basic -> basic.disable());  // 禁用HTTP Basic认证

        return http.build();
    }
}
