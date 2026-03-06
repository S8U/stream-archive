package com.github.s8u.streamarchive.security

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.exception.BusinessException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UserDetails

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties

    @BeforeEach
    fun setUp() {
        jwtProperties = JwtProperties(
            secret = "myTestSecretKeyThatIsAtLeast256BitsLongForHmacSha256AlgorithmToWork!",
            accessTokenExpiration = 3600000L,  // 1시간
            refreshTokenExpiration = 604800000L, // 7일
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
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
    }

    @Nested
    @DisplayName("generateAccessToken")
    inner class GenerateAccessToken {

        @Test
        @DisplayName("액세스 토큰을 생성한다")
        fun generateToken() {
            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = jwtTokenProvider.generateAccessToken(userDetails)

            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    inner class GenerateRefreshToken {

        @Test
        @DisplayName("리프레시 토큰을 생성한다")
        fun generateToken() {
            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = jwtTokenProvider.generateRefreshToken(userDetails)

            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    inner class GetUsernameFromToken {

        @Test
        @DisplayName("토큰에서 사용자명을 추출한다")
        fun extractUsername() {
            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = jwtTokenProvider.generateAccessToken(userDetails)
            val username = jwtTokenProvider.getUsernameFromToken(token)

            assertEquals("testuser", username)
        }

        @Test
        @DisplayName("잘못된 토큰에서 사용자명 추출 시 예외가 발생한다")
        fun extractFromInvalidToken() {
            assertThrows(BusinessException::class.java) {
                jwtTokenProvider.getUsernameFromToken("invalid.token.here")
            }
        }
    }

    @Nested
    @DisplayName("validateToken")
    inner class ValidateToken {

        @Test
        @DisplayName("유효한 토큰은 true를 반환한다")
        fun validateValidToken() {
            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = jwtTokenProvider.generateAccessToken(userDetails)

            assertTrue(jwtTokenProvider.validateToken(token))
        }

        @Test
        @DisplayName("잘못된 토큰은 false를 반환한다")
        fun validateInvalidToken() {
            assertFalse(jwtTokenProvider.validateToken("invalid.token"))
        }

        @Test
        @DisplayName("빈 문자열은 false를 반환한다")
        fun validateEmptyToken() {
            assertFalse(jwtTokenProvider.validateToken(""))
        }

        @Test
        @DisplayName("만료된 토큰은 false를 반환한다")
        fun validateExpiredToken() {
            val expiredProperties = JwtProperties(
                secret = "myTestSecretKeyThatIsAtLeast256BitsLongForHmacSha256AlgorithmToWork!",
                accessTokenExpiration = 0L, // 즉시 만료
                refreshTokenExpiration = 0L,
                cookie = jwtProperties.cookie
            )
            val expiredProvider = JwtTokenProvider(expiredProperties)

            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = expiredProvider.generateAccessToken(userDetails)

            // 토큰이 즉시 만료되므로 false
            assertFalse(expiredProvider.validateToken(token))
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰은 false를 반환한다")
        fun validateTokenWithDifferentKey() {
            val otherProperties = JwtProperties(
                secret = "aDifferentSecretKeyThatIsAlsoLongEnoughForTheAlgorithm256BitsMinimum!!",
                accessTokenExpiration = 3600000L,
                refreshTokenExpiration = 604800000L,
                cookie = jwtProperties.cookie
            )
            val otherProvider = JwtTokenProvider(otherProperties)

            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val token = otherProvider.generateAccessToken(userDetails)

            assertFalse(jwtTokenProvider.validateToken(token))
        }
    }

    @Nested
    @DisplayName("액세스 토큰과 리프레시 토큰은 서로 다르다")
    inner class TokenDifference {

        @Test
        @DisplayName("같은 사용자에 대해 액세스 토큰과 리프레시 토큰은 다르다")
        fun differentTokens() {
            val userDetails = mock<UserDetails>()
            whenever(userDetails.username).thenReturn("testuser")

            val accessToken = jwtTokenProvider.generateAccessToken(userDetails)
            val refreshToken = jwtTokenProvider.generateRefreshToken(userDetails)

            assertNotEquals(accessToken, refreshToken)
        }
    }
}
