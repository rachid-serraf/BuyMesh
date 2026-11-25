package com.buy01.product.infrastructure.security;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                return chain.filter(exchange);
            }

            String userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            UserPrincipal principal = new UserPrincipal(userId, email, role);

            // Map role to Spring Security authority
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            return chain.filter(exchange);
        }
    }

    public static class UserPrincipal {
        private final String id;
        private final String email;
        private final String role;

        public UserPrincipal(String id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String id() {
            return id;
        }

        public String email() {
            return email;
        }

        public String role() {
            return role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserPrincipal)) {
                return false;
            }
            UserPrincipal that = (UserPrincipal) o;
            return java.util.Objects.equals(id, that.id)
                    && java.util.Objects.equals(email, that.email)
                    && java.util.Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, email, role);
        }

        @Override
        public String toString() {
            return "UserPrincipal{id='" + id + "', email='" + email + "', role='" + role + "'}";
        }
    }
}