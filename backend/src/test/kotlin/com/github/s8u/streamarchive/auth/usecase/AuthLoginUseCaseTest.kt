package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.service.AuthTokenIssueService
import com.github.s8u.streamarchive.auth.service.dto.AuthTokenIssueResult
import com.github.s8u.streamarchive.auth.usecase.dto.command.AuthLoginCommand
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.repository.UserRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals

class AuthLoginUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val authTokenIssueService = mockk<AuthTokenIssueService>()
    private val authLoginUseCase = AuthLoginUseCase(userRepository, passwordEncoder, authTokenIssueService)

    @Nested
    inner class Login {

        @Test
        fun `사용자를 찾을 수 없으면 예외를 던진다`() {
            every { userRepository.findByUsername(USERNAME) } returns null

            assertThrows<BusinessException> {
                authLoginUseCase.login(command())
            }
        }

        @Test
        fun `비밀번호가 일치하지 않으면 예외를 던진다`() {
            val user = user()
            every { userRepository.findByUsername(USERNAME) } returns user
            every { passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD) } returns false

            assertThrows<BusinessException> {
                authLoginUseCase.login(command())
            }
        }

        @Test
        fun `로그인에 성공하면 토큰을 발급하고 마지막 로그인 일시를 갱신한다`() {
            val user = user()
            every { userRepository.findByUsername(USERNAME) } returns user
            every { passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD) } returns true
            every { user.login() } just Runs
            every { authTokenIssueService.issue(USER_ID, USERNAME) } returns AuthTokenIssueResult(ACCESS_TOKEN, REFRESH_TOKEN)

            val result = authLoginUseCase.login(command())

            assertEquals(ACCESS_TOKEN, result.accessToken)
            assertEquals(REFRESH_TOKEN, result.refreshToken)
            verify { user.login() }
            verify { authTokenIssueService.issue(USER_ID, USERNAME) }
        }
    }

    private fun command(): AuthLoginCommand {
        return AuthLoginCommand(username = USERNAME, password = PASSWORD)
    }

    private fun user(): User {
        val user = mockk<User>()
        every { user.id } returns USER_ID
        every { user.username } returns USERNAME
        every { user.password } returns ENCODED_PASSWORD
        return user
    }

    companion object {
        private const val USERNAME = "test-user"
        private const val PASSWORD = "raw-password"
        private const val ENCODED_PASSWORD = "encoded-password"
        private const val USER_ID = 1L
        private const val ACCESS_TOKEN = "access-token"
        private const val REFRESH_TOKEN = "refresh-token"
    }

}
