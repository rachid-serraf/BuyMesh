package com.buy01.order.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.buy01.order.common.Role;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, java.io.IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    UUID userId = jwtUtil.extractUserId(token);
                    String email = jwtUtil.extractEmail(token);
                    Role role = jwtUtil.extractRole(token);

                    UserPrincipal principal = new UserPrincipal(userId, email, role);

                    // Map role to Spring Security authority
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()));

                    UsernamePasswordAuthenticationToken authentication
                            = new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token validation failed, continue without authentication
            }
        }

        filterChain.doFilter(request, response);
    }

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    public static class UserPrincipal {

        public final UUID id;
        public final String email;
        public final Role role;
    }
}