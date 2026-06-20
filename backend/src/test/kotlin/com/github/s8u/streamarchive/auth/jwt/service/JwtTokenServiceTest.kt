package com.github.s8u.streamarchive.auth.jwt.service

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.global.exception.BusinessException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtTokenServiceTest {

    private val jwtTokenService = JwtTokenService(jwtProperties())

    // 다른 secret으로 서명한 토큰은 검증에 실패해야 한다
    private val otherJwtTokenService = JwtTokenService(jwtProperties(secret = "another-test-secret-key-1234567890"))

    @Nested
    inner class GenerateToken {

        @Test
        fun `발급한 액세스 토큰은 같은 서비스에서 검증에 성공한다`() {
            val token = jwtTokenService.generateAccessToken(userDetails())

            assertTrue(jwtTokenService.validateToken(token))
        }

        @Test
        fun `발급한 토큰에서 사용자명을 다시 꺼낼 수 있다`() {
            val token = jwtTokenService.generateAccessToken(userDetails())

            assertEquals(USERNAME, jwtTokenService.getUsernameFromToken(token))
        }
    }

    @Nested
    inner class ValidateToken {

        @Test
        fun `유효한 토큰은 true를 반환한다`() {
            val token = jwtTokenService.generateRefreshToken(userDetails())

            assertTrue(jwtTokenService.validateToken(token))
        }

        @Test
        fun `형식이 잘못된 토큰은 false를 반환한다`() {
            assertFalse(jwtTokenService.validateToken("not-a-jwt-token"))
        }

        @Test
        fun `다른 secret으로 서명한 토큰은 false를 반환한다`() {
            val token = otherJwtTokenService.generateAccessToken(userDetails())

            assertFalse(jwtTokenService.validateToken(token))
        }

        @Test
        fun `만료된 토큰은 false를 반환한다`() {
            // 만료 시간을 음수로 줘서 발급 즉시 만료된 토큰을 만든다
            val expiredService = JwtTokenService(jwtProperties(accessTokenExpiration = -1000L))
            val token = expiredService.generateAccessToken(userDetails())

            assertFalse(jwtTokenService.validateToken(token))
        }
    }

    @Nested
    inner class GetUsernameFromToken {

        @Test
        fun `잘못된 토큰을 파싱하면 401 예외를 던진다`() {
            val exception = assertThrows<BusinessException> {
                jwtTokenService.getUsernameFromToken("invalid-token")
            }

            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }
    }

    private fun userDetails(): UserDetails {
        return User.withUsername(USERNAME)
            .password("password")
            .authorities(emptyList())
            .build()
    }

    private fun jwtProperties(
        secret: String = "test-secret-key-for-jwt-token-1234567890",
        accessTokenExpiration: Long = 3_600_000L
    ): JwtProperties {
        return JwtProperties(
            secret = secret,
            accessTokenExpiration = accessTokenExpiration,
            refreshTokenExpiration = 86_400_000L,
            cookie = JwtProperties.CookieProperties(
                secure = false,
                sameSite = "Lax",
                httpOnly = true,
                path = "/",
                domain = "localhost",
                accessTokenName = "access_token",
                refreshTokenName = "refresh_token"
            )
        )
    }

    companion object {
        private const val USERNAME = "test-user"
    }

}
