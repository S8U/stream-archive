package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserPasswordUpdateCommand
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder

class UserPasswordUpdateUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val currentUserService = mockk<CurrentUserService>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userPasswordUpdateUseCase = UserPasswordUpdateUseCase(userRepository, currentUserService, passwordEncoder)

    @Nested
    inner class Update {

        @Test
        fun `로그인한 사용자가 없으면 예외를 던진다`() {
            every { currentUserService.getCurrentUser() } returns null

            assertThrows<BusinessException> {
                userPasswordUpdateUseCase.update(command())
            }
        }

        @Test
        fun `현재 비밀번호가 일치하지 않으면 예외를 던진다`() {
            val user = user()
            every { currentUserService.getCurrentUser() } returns user
            every { passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_CURRENT_PASSWORD) } returns false

            assertThrows<BusinessException> {
                userPasswordUpdateUseCase.update(command())
            }
        }

        @Test
        fun `현재 비밀번호가 맞으면 새 비밀번호로 변경하고 저장한다`() {
            val user = user()
            every { currentUserService.getCurrentUser() } returns user
            every { passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_CURRENT_PASSWORD) } returns true
            every { passwordEncoder.encode(NEW_PASSWORD) } returns ENCODED_NEW_PASSWORD
            every { user.changePassword(ENCODED_NEW_PASSWORD) } just Runs
            every { userRepository.save(user) } returns user

            userPasswordUpdateUseCase.update(command())

            verify { user.changePassword(ENCODED_NEW_PASSWORD) }
            verify { userRepository.save(user) }
        }
    }

    private fun command(): UserPasswordUpdateCommand {
        return UserPasswordUpdateCommand(currentPassword = CURRENT_PASSWORD, newPassword = NEW_PASSWORD)
    }

    private fun user(): User {
        val user = mockk<User>()
        every { user.password } returns ENCODED_CURRENT_PASSWORD
        return user
    }

    companion object {
        private const val CURRENT_PASSWORD = "current-password"
        private const val NEW_PASSWORD = "new-password"
        private const val ENCODED_CURRENT_PASSWORD = "encoded-current-password"
        private const val ENCODED_NEW_PASSWORD = "encoded-new-password"
    }

}
