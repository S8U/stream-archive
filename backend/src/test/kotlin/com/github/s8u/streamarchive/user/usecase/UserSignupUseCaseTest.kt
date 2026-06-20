package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserSignupCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals

class UserSignupUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userSignupUseCase = UserSignupUseCase(userRepository, passwordEncoder)

    @Nested
    inner class Signup {

        @Test
        fun `이미 존재하는 아이디면 예외를 던지고 저장하지 않는다`() {
            every { userRepository.findByUsername(USERNAME) } returns mockk<User>()

            val exception = assertThrows<BusinessException> {
                userSignupUseCase.signup(command())
            }

            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
            verify(exactly = 0) { userRepository.save(any()) }
        }

        @Test
        fun `중복이 없으면 비밀번호를 인코딩해 사용자를 저장한다`() {
            every { userRepository.findByUsername(USERNAME) } returns null
            every { passwordEncoder.encode(PASSWORD) } returns ENCODED_PASSWORD
            val userSlot = slot<User>()
            every { userRepository.save(capture(userSlot)) } answers { userSlot.captured }

            userSignupUseCase.signup(command())

            verify { passwordEncoder.encode(PASSWORD) }
            verify { userRepository.save(any()) }
            val saved = userSlot.captured
            assertEquals(USERNAME, saved.username)
            assertEquals(NAME, saved.name)
            assertEquals(ENCODED_PASSWORD, saved.password)
            assertEquals(Role.USER, saved.role)
        }
    }

    private fun command(): UserSignupCommand {
        return UserSignupCommand(username = USERNAME, name = NAME, password = PASSWORD)
    }

    companion object {
        private const val USERNAME = "test-user"
        private const val NAME = "테스트"
        private const val PASSWORD = "raw-password"
        private const val ENCODED_PASSWORD = "encoded-password"
    }

}
