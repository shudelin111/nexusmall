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

    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

    public SecurityConfig(JwtAuthenticationWebFilter jwtAuthenticationWebFilter) {
        this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable).formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).authorizeExchange(exchanges -> exchanges
                        // 允许公开访问的路径
                        .pathMatchers("/actuator/**").permitAll().pathMatchers("/auth/login", "/auth/register")
                        .permitAll().pathMatchers("/auth/validate").authenticated().pathMatchers("/auth/**")
                        .hasAnyRole("ADMIN")

                        // 商品服务路径
                        .pathMatchers("/product/list", "/product/view").permitAll().pathMatchers("/product/**")
                        .hasAnyAuthority("product:list", "product:add", "product:edit", "product:delete")

                        // 订单服务路径
                        .pathMatchers("/order/**").authenticated()

                        // 其他路径需要认证
                        .anyExchange().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }).accessDeniedHandler((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                })).addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
    }

}
