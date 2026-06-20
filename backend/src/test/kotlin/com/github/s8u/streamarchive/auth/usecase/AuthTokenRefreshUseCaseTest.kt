package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.entity.RefreshToken
import com.github.s8u.streamarchive.auth.jwt.service.JwtTokenService
import com.github.s8u.streamarchive.auth.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.auth.service.AuthTokenIssueService
import com.github.s8u.streamarchive.auth.service.dto.AuthTokenIssueResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.util.RequestUtils
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.repository.UserRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.Runs
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AuthTokenRefreshUseCaseTest {

    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val userRepository = mockk<UserRepository>()
    private val jwtTokenService = mockk<JwtTokenService>()
    private val authTokenIssueService = mockk<AuthTokenIssueService>()
    private val authTokenRefreshUseCase = AuthTokenRefreshUseCase(
        refreshTokenRepository,
        userRepository,
        jwtTokenService,
        authTokenIssueService
    )

    @AfterEach
    fun tearDown() {
        unmockkObject(RequestUtils)
    }

    @Nested
    inner class Refresh {

        @Test
        fun `토큰이 무효하면 예외를 던진다`() {
            every { jwtTokenService.validateToken(REFRESH_TOKEN) } returns false

            assertThrows<BusinessException> {
                authTokenRefreshUseCase.refresh(REFRESH_TOKEN)
            }
        }

        @Test
        fun `저장된 토큰이 없으면 예외를 던진다`() {
            every { jwtTokenService.validateToken(REFRESH_TOKEN) } returns true
            every { refreshTokenRepository.findByToken(REFRESH_TOKEN) } returns null

            assertThrows<BusinessException> {
                authTokenRefreshUseCase.refresh(REFRESH_TOKEN)
            }
        }

        @Test
        fun `저장된 토큰이 만료됐으면 예외를 던진다`() {
            val storedToken = storedToken(expiresAt = LocalDateTime.now().minusDays(1))
            every { jwtTokenService.validateToken(REFRESH_TOKEN) } returns true
            every { refreshTokenRepository.findByToken(REFRESH_TOKEN) } returns storedToken

            assertThrows<BusinessException> {
                authTokenRefreshUseCase.refresh(REFRESH_TOKEN)
            }
        }

        @Test
        fun `사용자를 찾을 수 없으면 예외를 던진다`() {
            val storedToken = storedToken(expiresAt = LocalDateTime.now().plusDays(1))
            every { jwtTokenService.validateToken(REFRESH_TOKEN) } returns true
            every { refreshTokenRepository.findByToken(REFRESH_TOKEN) } returns storedToken
            every { jwtTokenService.getUsernameFromToken(REFRESH_TOKEN) } returns USERNAME
            every { userRepository.findByUsername(USERNAME) } returns null

            assertThrows<BusinessException> {
                authTokenRefreshUseCase.refresh(REFRESH_TOKEN)
            }
        }

        @Test
        fun `갱신에 성공하면 기존 토큰을 무효화하고 새 토큰을 발급한다`() {
            mockkObject(RequestUtils)
            every { RequestUtils.getClientIp() } returns CLIENT_IP

            val storedToken = storedToken(expiresAt = LocalDateTime.now().plusDays(1))
            val user = mockk<User>()
            every { user.id } returns USER_ID
            every { jwtTokenService.validateToken(REFRESH_TOKEN) } returns true
            every { refreshTokenRepository.findByToken(REFRESH_TOKEN) } returns storedToken
            every { jwtTokenService.getUsernameFromToken(REFRESH_TOKEN) } returns USERNAME
            every { userRepository.findByUsername(USERNAME) } returns user
            every { storedToken.softDelete(USER_ID, CLIENT_IP) } just Runs
            every { authTokenIssueService.issue(USER_ID, USERNAME) } returns AuthTokenIssueResult(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN)

            val result = authTokenRefreshUseCase.refresh(REFRESH_TOKEN)

            assertEquals(NEW_ACCESS_TOKEN, result.accessToken)
            assertEquals(NEW_REFRESH_TOKEN, result.refreshToken)
            verify { storedToken.softDelete(USER_ID, CLIENT_IP) }
            verify { authTokenIssueService.issue(USER_ID, USERNAME) }
        }
    }

    private fun storedToken(expiresAt: LocalDateTime): RefreshToken {
        val token = mockk<RefreshToken>()
        every { token.userId } returns USER_ID
        every { token.expiresAt } returns expiresAt
        return token
    }

    companion object {
        private const val REFRESH_TOKEN = "refresh-token"
        private const val NEW_ACCESS_TOKEN = "new-access-token"
        private const val NEW_REFRESH_TOKEN = "new-refresh-token"
        private const val USERNAME = "test-user"
        private const val USER_ID = 1L
        private const val CLIENT_IP = "127.0.0.1"
    }

}
