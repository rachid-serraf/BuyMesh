package com.buy01.media.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.buy01.media.infrastructure.security.JwtAuthenticationFilter.UserPrincipal;

class JwtAuthenticationFilterTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWithValidBearerToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer valid-token");
        given(jwtUtil.validateToken("valid-token")).willReturn(true);
        given(jwtUtil.extractUserId("valid-token")).willReturn("user-1");
        given(jwtUtil.extractEmail("valid-token")).willReturn("seller@buy01.com");
        given(jwtUtil.extractRole("valid-token")).willReturn("seller");

        filter.doFilterInternal(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(
                new UserPrincipal("user-1", "seller@buy01.com", "seller"));
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_SELLER");
        verify(chain).doFilter(request, response);
    }

    @Test
    void continuesWithoutAuthenticationWhenHeaderIsMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void continuesWithoutAuthenticationForNonBearerHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Basic abc");

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void continuesWithoutAuthenticationWhenTokenIsInvalid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer invalid-token");
        given(jwtUtil.validateToken("invalid-token")).willReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void swallowsExceptionsDuringTokenProcessing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer broken-token");
        given(jwtUtil.validateToken("broken-token")).willThrow(new RuntimeException("broken"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void userPrincipalExposesFieldsAndValueSemantics() {
        UserPrincipal principal = new UserPrincipal("user-1", "seller@buy01.com", "SELLER");
        UserPrincipal same = new UserPrincipal("user-1", "seller@buy01.com", "SELLER");
        UserPrincipal different = new UserPrincipal("user-2", "buyer@buy01.com", "BUYER");
        UserPrincipal differentEmail = new UserPrincipal("user-1", "buyer@buy01.com", "SELLER");
        UserPrincipal differentRole = new UserPrincipal("user-1", "seller@buy01.com", "BUYER");

        assertThat(principal.getId()).isEqualTo("user-1");
        assertThat(principal.getEmail()).isEqualTo("seller@buy01.com");
        assertThat(principal.getRole()).isEqualTo("SELLER");
        assertThat(principal.id()).isEqualTo("user-1");
        assertThat(principal.email()).isEqualTo("seller@buy01.com");
        assertThat(principal.role()).isEqualTo("SELLER");
        assertThat(principal).isEqualTo(principal).isEqualTo(same)
                .isNotEqualTo(different)
                .isNotEqualTo(differentEmail)
                .isNotEqualTo(differentRole);
        assertThat(principal).isNotEqualTo("not-a-principal");
        assertThat(principal.hashCode()).isEqualTo(same.hashCode());
        assertThat(principal.toString()).contains("user-1", "seller@buy01.com", "SELLER");
    }
}
