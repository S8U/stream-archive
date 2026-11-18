package com.github.s8u.streamarchive.auth.jwt.filter

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.auth.jwt.service.JwtTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val jwtProperties: JwtProperties
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = extractAccessToken(request)

        // 액세스 토큰이 유효할 때만 인증을 설정한다
        // 없거나 유효하지 않으면 인증 없이 넘기고, 보호된 API라면 Spring Security가 401이나 403을 돌려준다
        if (accessToken != null && jwtTokenService.validateToken(accessToken)) {
            setAuthentication(accessToken, request)
        }

        filterChain.doFilter(request, response)
    }

    // 쿠키 우선, 없으면 Authorization 헤더에서 액세스 토큰을 읽는다
    // 브라우저는 쿠키로 보내고, 모바일 앱과 Swagger는 Bearer 헤더로 보낸다
    // 인증 방식 이름(Bearer)은 대소문자를 가리지 않는다(RFC 7235)
    private fun extractAccessToken(request: HttpServletRequest): String? {
        val cookieToken = request.cookies
            ?.find { it.name == jwtProperties.cookie.accessTokenName }
            ?.value
        val headerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)

        return cookieToken ?: headerToken
    }

    private fun setAuthentication(token: String, request: HttpServletRequest) {
        val username = jwtTokenService.getUsernameFromToken(token)
        val userDetails = userDetailsService.loadUserByUsername(username)

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
        logger.debug("JwtAuthenticationFilter: Set authentication for user: $username")
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

}
