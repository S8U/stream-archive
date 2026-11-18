package com.github.s8u.streamarchive.auth.jwt.filter

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.auth.jwt.service.JwtTokenService
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JwtAuthenticationFilterTest {

    private val jwtProperties = jwtProperties()
    private val jwtTokenService = JwtTokenService(jwtProperties)
    private val userDetailsService = mockk<UserDetailsService>()
    private val jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenService, userDetailsService, jwtProperties)

    @AfterEach
    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `쿠키의 액세스 토큰으로 인증한다`() {
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken(COOKIE_USERNAME)))

        doFilter(request)

        assertEquals(COOKIE_USERNAME, getAuthenticatedUsername())
    }

    @Test
    fun `쿠키가 없으면 Bearer 헤더의 액세스 토큰으로 인증한다`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken(HEADER_USERNAME)}")

        doFilter(request)

        assertEquals(HEADER_USERNAME, getAuthenticatedUsername())
    }

    @Test
    fun `쿠키와 Bearer 헤더가 모두 있으면 쿠키의 토큰을 쓴다`() {
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken(COOKIE_USERNAME)))
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken(HEADER_USERNAME)}")

        doFilter(request)

        assertEquals(COOKIE_USERNAME, getAuthenticatedUsername())
    }

    @Test
    fun `Bearer가 아닌 Authorization 헤더는 무시한다`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic ${accessToken(HEADER_USERNAME)}")

        doFilter(request)

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `유효하지 않은 토큰이면 인증 없이 넘긴다`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt-token")

        doFilter(request)

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    private fun doFilter(request: MockHttpServletRequest) {
        jwtAuthenticationFilter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
    }

    private fun getAuthenticatedUsername(): String? {
        val principal = SecurityContextHolder.getContext().authentication?.principal as UserDetails?
        return principal?.username
    }

    private fun accessToken(username: String): String {
        val userDetails = userDetails(username)
        every { userDetailsService.loadUserByUsername(username) } returns userDetails

        return jwtTokenService.generateAccessToken(userDetails)
    }

    private fun userDetails(username: String): UserDetails {
        return User.withUsername(username)
            .password("password")
            .authorities(emptyList())
            .build()
    }

    private fun jwtProperties(): JwtProperties {
        return JwtProperties(
            secret = "test-secret-key-for-jwt-token-1234567890",
            accessTokenExpiration = 3_600_000L,
            refreshTokenExpiration = 86_400_000L,
            cookie = JwtProperties.CookieProperties(
                secure = false,
                sameSite = "Lax",
                httpOnly = true,
                path = "/",
                domain = "localhost",
                accessTokenName = ACCESS_TOKEN_COOKIE_NAME,
                refreshTokenName = "refresh_token"
            )
        )
    }

    companion object {
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val COOKIE_USERNAME = "cookie-user"
        private const val HEADER_USERNAME = "header-user"
    }

}
