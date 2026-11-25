package com.buy01.user.infrastructure.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.port.out.TokenGeneratorPort;

import lombok.Getter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenGeneratorPort tokenGenerator;

    public JwtAuthenticationFilter(TokenGeneratorPort tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, java.io.IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (tokenGenerator.validateToken(token)) {
                    String email = tokenGenerator.extractEmail(token);
                    UUID userId = tokenGenerator.extractUserId(token);
                    Role role = tokenGenerator.extractRole(token);

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
    public static class UserPrincipal {

        private final UUID id;
        private final String email;
        private final Role role;

        public UserPrincipal(UUID id, String email, Role role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }
    }
}
