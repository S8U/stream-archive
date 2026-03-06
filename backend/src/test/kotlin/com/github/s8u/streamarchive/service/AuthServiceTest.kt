package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.dto.LoginRequest
import com.github.s8u.streamarchive.dto.SignupRequest
import com.github.s8u.streamarchive.entity.RefreshToken
import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.repository.UserRepository
import com.github.s8u.streamarchive.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    lateinit var jwtProperties: JwtProperties

    @Mock
    lateinit var userDetailsService: UserDetailsService

    @InjectMocks
    lateinit var authService: AuthService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 1L,
            uuid = "test-uuid",
            username = "testuser",
            name = "Test User",
            password = "encoded_password",
            role = Role.USER
        )
    }

    @Nested
    @DisplayName("login")
    inner class Login {

        @Test
        @DisplayName("올바른 자격 증명으로 로그인 시 토큰을 반환한다")
        fun loginWithValidCredentials() {
            val request = LoginRequest(username = "testuser", password = "password123")
            val userDetails = mock<UserDetails>()

            whenever(userRepository.findByUsername("testuser")).thenReturn(testUser)
            whenever(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true)
            whenever(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails)
            whenever(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access_token")
            whenever(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh_token")
            whenever(jwtProperties.refreshTokenExpiration).thenReturn(604800000L)
            whenever(refreshTokenRepository.save(any<RefreshToken>())).thenAnswer { it.arguments[0] }

            val response = authService.login(request)

            assertEquals("access_token", response.accessToken)
            assertEquals("refresh_token", response.refreshToken)
            assertNotNull(testUser.lastLoginAt)
            verify(refreshTokenRepository).save(any<RefreshToken>())
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시 예외가 발생한다")
        fun loginWithNonExistentUser() {
            val request = LoginRequest(username = "nonexistent", password = "password")

            whenever(userRepository.findByUsername("nonexistent")).thenReturn(null)

            val exception = assertThrows(BusinessException::class.java) {
                authService.login(request)
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        fun loginWithWrongPassword() {
            val request = LoginRequest(username = "testuser", password = "wrong")

            whenever(userRepository.findByUsername("testuser")).thenReturn(testUser)
            whenever(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false)

            val exception = assertThrows(BusinessException::class.java) {
                authService.login(request)
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }
    }

    @Nested
    @DisplayName("signup")
    inner class Signup {

        @Test
        @DisplayName("새 사용자 등록에 성공한다")
        fun signupSuccess() {
            val request = SignupRequest(username = "newuser", name = "New User", password = "password123")

            whenever(userRepository.findByUsername("newuser")).thenReturn(null)
            whenever(passwordEncoder.encode("password123")).thenReturn("encoded_password")
            whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }

            assertDoesNotThrow { authService.signup(request) }
            verify(userRepository).save(argThat<User> {
                username == "newuser" && name == "New User" && role == Role.USER
            })
        }

        @Test
        @DisplayName("이미 존재하는 사용자명으로 등록 시 예외가 발생한다")
        fun signupWithDuplicateUsername() {
            val request = SignupRequest(username = "testuser", name = "Test", password = "password")

            whenever(userRepository.findByUsername("testuser")).thenReturn(testUser)

            val exception = assertThrows(BusinessException::class.java) {
                authService.signup(request)
            }
            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        }
    }

    @Nested
    @DisplayName("refresh")
    inner class Refresh {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새 토큰을 발급한다")
        fun refreshWithValidToken() {
            val storedToken = RefreshToken(
                id = 1L,
                userId = 1L,
                token = "valid_refresh_token",
                expiresAt = LocalDateTime.now().plusDays(7)
            )
            val userDetails = mock<UserDetails>()

            whenever(jwtTokenProvider.validateToken("valid_refresh_token")).thenReturn(true)
            whenever(refreshTokenRepository.findByToken("valid_refresh_token")).thenReturn(storedToken)
            whenever(jwtTokenProvider.getUsernameFromToken("valid_refresh_token")).thenReturn("testuser")
            whenever(userRepository.findByUsername("testuser")).thenReturn(testUser)
            whenever(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails)
            whenever(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("new_access")
            whenever(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("new_refresh")
            whenever(jwtProperties.refreshTokenExpiration).thenReturn(604800000L)
            whenever(refreshTokenRepository.save(any<RefreshToken>())).thenAnswer { it.arguments[0] }

            val response = authService.refresh("valid_refresh_token")

            assertEquals("new_access", response.accessToken)
            assertEquals("new_refresh", response.refreshToken)
            assertFalse(storedToken.isActive)
        }

        @Test
        @DisplayName("유효하지 않은 JWT 리프레시 토큰으로 갱신 시 예외가 발생한다")
        fun refreshWithInvalidJwt() {
            whenever(jwtTokenProvider.validateToken("invalid_token")).thenReturn(false)

            val exception = assertThrows(BusinessException::class.java) {
                authService.refresh("invalid_token")
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }

        @Test
        @DisplayName("DB에 없는 리프레시 토큰으로 갱신 시 예외가 발생한다")
        fun refreshWithTokenNotInDb() {
            whenever(jwtTokenProvider.validateToken("orphan_token")).thenReturn(true)
            whenever(refreshTokenRepository.findByToken("orphan_token")).thenReturn(null)

            val exception = assertThrows(BusinessException::class.java) {
                authService.refresh("orphan_token")
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다")
        fun refreshWithExpiredToken() {
            val storedToken = RefreshToken(
                id = 1L,
                userId = 1L,
                token = "expired_token",
                expiresAt = LocalDateTime.now().minusDays(1)
            )

            whenever(jwtTokenProvider.validateToken("expired_token")).thenReturn(true)
            whenever(refreshTokenRepository.findByToken("expired_token")).thenReturn(storedToken)

            val exception = assertThrows(BusinessException::class.java) {
                authService.refresh("expired_token")
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }
    }

    @Nested
    @DisplayName("logout")
    inner class Logout {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 로그아웃 시 토큰을 비활성화한다")
        fun logoutWithValidToken() {
            val storedToken = RefreshToken(
                id = 1L,
                userId = 1L,
                token = "refresh_token",
                expiresAt = LocalDateTime.now().plusDays(7)
            )

            whenever(refreshTokenRepository.findByToken("refresh_token")).thenReturn(storedToken)

            authService.logout("refresh_token")

            assertFalse(storedToken.isActive)
            assertNotNull(storedToken.deletedAt)
        }

        @Test
        @DisplayName("DB에 없는 토큰으로 로그아웃해도 예외가 발생하지 않는다")
        fun logoutWithNonExistentToken() {
            whenever(refreshTokenRepository.findByToken("unknown_token")).thenReturn(null)

            assertDoesNotThrow { authService.logout("unknown_token") }
        }
    }
}
