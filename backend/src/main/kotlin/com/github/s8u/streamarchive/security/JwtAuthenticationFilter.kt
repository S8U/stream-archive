package com.github.s8u.streamarchive.security

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.service.AuthService
import com.github.s8u.streamarchive.service.JwtCookieService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
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
    private val jwtProperties: JwtProperties,
    @Lazy private val authService: AuthService,
    private val jwtCookieService: JwtCookieService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = extractAccessTokenFromCookie(request)

        // 인증
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            setAuthentication(accessToken, request)
        }
        // Access Token 재발급
        else {
            logger.debug("Access token invalid, attempting auto-refresh")

            val refreshToken = extractRefreshTokenFromCookie(request)
            if (refreshToken != null) {
                try {
                    val newTokens = authService.refresh(refreshToken)

                    // 새 쿠키 설정
                    jwtCookieService.setAccessTokenCookie(response, newTokens.accessToken)
                    jwtCookieService.setRefreshTokenCookie(response, newTokens.refreshToken)

                    // 새 AT로 인증
                    setAuthentication(newTokens.accessToken, request)

                    logger.info("Access token auto-refreshed successfully")
                } catch (refreshException: Exception) {
                    logger.debug("Auto-refresh failed", refreshException)
                    SecurityContextHolder.clearContext()
                }
            } else {
                logger.debug("No refresh token available for auto-refresh")
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractAccessTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find {
            it.name == jwtProperties.cookie.accessTokenName
        }?.value
    }

    private fun extractRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find {
            it.name == jwtProperties.cookie.refreshTokenName
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
        logger.debug("Set authentication for user: $username")
    }

}
