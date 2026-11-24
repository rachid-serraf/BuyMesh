package com.buy01.order.security

import java.util.UUID

import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

import com.buy01.order.common.Role

import spock.lang.Specification

class JwtAuthenticationFilterSpec extends Specification {

    private JwtUtil jwtUtil
    private JwtAuthenticationFilter filter

    def setup() {
        jwtUtil = Mock()
        filter = new JwtAuthenticationFilter(jwtUtil)
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "request without authorization header passes through unauthenticated"() {
        given:
        def request = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilterInternal(request, response, chain)

        then:
        0 * jwtUtil._
        SecurityContextHolder.context.authentication == null
        chain.request == request
    }

    def "request with non-bearer header passes through unauthenticated"() {
        given:
        def request = new MockHttpServletRequest()
        request.addHeader("Authorization", "Basic abc")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilterInternal(request, response, chain)

        then:
        0 * jwtUtil._
        SecurityContextHolder.context.authentication == null
        chain.request == request
    }

    def "valid token populates the security context"() {
        given:
        def userId = UUID.randomUUID()
        def request = new MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer valid-token")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilterInternal(request, response, chain)

        then:
        1 * jwtUtil.validateToken("valid-token") >> true
        1 * jwtUtil.extractUserId("valid-token") >> userId
        1 * jwtUtil.extractEmail("valid-token") >> "buyer@example.com"
        1 * jwtUtil.extractRole("valid-token") >> Role.CLIENT

        def authentication = SecurityContextHolder.context.authentication
        authentication != null
        authentication.principal == new JwtAuthenticationFilter.UserPrincipal(userId, "buyer@example.com", Role.CLIENT)
        authentication.authorities*.authority == ["ROLE_CLIENT"]
        chain.request == request
    }

    def "invalid token leaves the security context empty"() {
        given:
        def request = new MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer invalid-token")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilterInternal(request, response, chain)

        then:
        1 * jwtUtil.validateToken("invalid-token") >> false
        0 * jwtUtil.extractUserId(_)
        SecurityContextHolder.context.authentication == null
        chain.request == request
    }

    def "token extraction failure is swallowed and the chain continues"() {
        given:
        def request = new MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer broken-token")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilterInternal(request, response, chain)

        then:
        1 * jwtUtil.validateToken("broken-token") >> true
        1 * jwtUtil.extractUserId("broken-token") >> { throw new RuntimeException("broken") }
        SecurityContextHolder.context.authentication == null
        chain.request == request
    }

    def "UserPrincipal exposes constructor fields and value semantics"() {
        given:
        def id = UUID.randomUUID()
        def first = new JwtAuthenticationFilter.UserPrincipal(id, "buyer@example.com", Role.CLIENT)
        def same = new JwtAuthenticationFilter.UserPrincipal(id, "buyer@example.com", Role.CLIENT)
        def different = new JwtAuthenticationFilter.UserPrincipal(id, "seller@example.com", Role.SELLER)

        expect:
        first.id() == id
        first.email() == "buyer@example.com"
        first.role() == Role.CLIENT
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("buyer@example.com")
    }

    def "UserPrincipal covers null and populated equality and hash branches"() {
        given:
        def empty = new JwtAuthenticationFilter.UserPrincipal(null, null, null)
        def populated = new JwtAuthenticationFilter.UserPrincipal(
                UUID.randomUUID(), "buyer@example.com", Role.CLIENT)
        def populatedCopy = new JwtAuthenticationFilter.UserPrincipal(
                populated.id(), "buyer@example.com", Role.CLIENT)

        expect:
        empty == new JwtAuthenticationFilter.UserPrincipal(null, null, null)
        empty.hashCode() == new JwtAuthenticationFilter.UserPrincipal(null, null, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
