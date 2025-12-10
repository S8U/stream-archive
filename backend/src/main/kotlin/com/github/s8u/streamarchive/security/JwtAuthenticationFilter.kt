package com.github.s8u.streamarchive.security

import com.github.s8u.streamarchive.config.properties.JwtProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService,
    private val jwtProperties: JwtProperties
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = extractAccessTokenFromCookie(request)

        // Access Token이 유효하면 인증 설정
        // 유효하지 않으면 인증 없이 진행 → Spring Security가 401/403 반환
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            setAuthentication(accessToken, request)
        }

        filterChain.doFilter(request, response)
    }

    private fun extractAccessTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find {
            it.name == jwtProperties.cookie.accessTokenName
        }?.value
    }

    private fun setAuthentication(token: String, request: HttpServletRequest) {
        val username = jwtTokenProvider.getUsernameFromToken(token)
        val userDetails = userDetailsService.loadUserByUsername(username)

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
        log.debug("Set authentication for user: $username")
    }
}
