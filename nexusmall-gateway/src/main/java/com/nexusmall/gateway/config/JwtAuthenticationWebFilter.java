package com.nexusmall.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtConfig jwtConfig;

    public JwtAuthenticationWebFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    token,
                    AuthorityUtils.createAuthorityList("ROLE_USER")
            );
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } catch (Exception ex) {
            return chain.filter(exchange);
        }
    }
}
