package com.nexusmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    // 注意: JwtAuthenticationWebFilter 已废弃,使用 JwtAuthGlobalFilter (RSA-256) 替代
    // private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

    // public SecurityConfig(JwtAuthenticationWebFilter jwtAuthenticationWebFilter) {
    //     this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
    // }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // 注意: 所有认证由 JwtAuthGlobalFilter 处理 (RS256)
                // Spring Security 这里只做最基础的配置,不干预认证逻辑
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())  // 允许所有请求,由 JwtAuthGlobalFilter 控制
                .build();
    }

}
