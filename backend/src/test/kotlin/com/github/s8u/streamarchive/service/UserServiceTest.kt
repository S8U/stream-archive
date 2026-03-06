package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminUserUpdateRequest
import com.github.s8u.streamarchive.dto.UserUpdatePasswordRequest
import com.github.s8u.streamarchive.dto.UserUpdateRequest
import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserRepository
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
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var authenticationService: AuthenticationService

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    lateinit var userService: UserService

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
    @DisplayName("getForAdmin")
    inner class GetForAdmin {

        @Test
        @DisplayName("존재하는 사용자를 조회한다")
        fun getExistingUser() {
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))

            val response = userService.getForAdmin(1L)

            assertEquals(1L, response.id)
            assertEquals("testuser", response.username)
            assertEquals("Test User", response.name)
            assertEquals(Role.USER, response.role)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
        fun getNonExistentUser() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows(BusinessException::class.java) {
                userService.getForAdmin(999L)
            }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }
    }

    @Nested
    @DisplayName("updateForAdmin")
    inner class UpdateForAdmin {

        @Test
        @DisplayName("사용자 이름과 역할을 수정한다")
        fun updateNameAndRole() {
            val request = AdminUserUpdateRequest(name = "Updated Name", role = Role.ADMIN)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))

            val response = userService.updateForAdmin(1L, request)

            assertEquals("Updated Name", response.name)
            assertEquals(Role.ADMIN, response.role)
        }

        @Test
        @DisplayName("null 값은 기존 값을 유지한다")
        fun updateWithNulls() {
            val request = AdminUserUpdateRequest(name = null, role = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))

            val response = userService.updateForAdmin(1L, request)

            assertEquals("Test User", response.name)
            assertEquals(Role.USER, response.role)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 수정 시 예외가 발생한다")
        fun updateNonExistentUser() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                userService.updateForAdmin(999L, AdminUserUpdateRequest(name = "test", role = null))
            }
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("사용자를 soft delete한다")
        fun softDeleteUser() {
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))

            userService.delete(1L)

            assertFalse(testUser.isActive)
            assertNotNull(testUser.deletedAt)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 예외가 발생한다")
        fun deleteNonExistentUser() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                userService.delete(999L)
            }
        }
    }

    @Nested
    @DisplayName("updateCurrentUser")
    inner class UpdateCurrentUser {

        @Test
        @DisplayName("현재 사용자의 이름을 수정한다")
        fun updateCurrentUserName() {
            val request = UserUpdateRequest(name = "New Name")
            whenever(authenticationService.getCurrentUser()).thenReturn(testUser)
            whenever(userRepository.save(any<User>())).thenReturn(testUser)

            val response = userService.updateCurrentUser(request)

            assertEquals("New Name", testUser.name)
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 수정 시 예외가 발생한다")
        fun updateWithoutLogin() {
            whenever(authenticationService.getCurrentUser()).thenReturn(null)

            val exception = assertThrows(BusinessException::class.java) {
                userService.updateCurrentUser(UserUpdateRequest(name = "test"))
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }
    }

    @Nested
    @DisplayName("updatePassword")
    inner class UpdatePassword {

        @Test
        @DisplayName("올바른 현재 비밀번호로 비밀번호를 변경한다")
        fun updatePasswordSuccess() {
            val request = UserUpdatePasswordRequest(currentPassword = "current", newPassword = "newpass")
            whenever(authenticationService.getCurrentUser()).thenReturn(testUser)
            whenever(passwordEncoder.matches("current", "encoded_password")).thenReturn(true)
            whenever(passwordEncoder.encode("newpass")).thenReturn("new_encoded")
            whenever(userRepository.save(any<User>())).thenReturn(testUser)

            assertDoesNotThrow { userService.updatePassword(request) }
            assertEquals("new_encoded", testUser.password)
        }

        @Test
        @DisplayName("잘못된 현재 비밀번호로 변경 시 예외가 발생한다")
        fun updatePasswordWithWrongCurrent() {
            val request = UserUpdatePasswordRequest(currentPassword = "wrong", newPassword = "newpass")
            whenever(authenticationService.getCurrentUser()).thenReturn(testUser)
            whenever(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false)

            val exception = assertThrows(BusinessException::class.java) {
                userService.updatePassword(request)
            }
            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 비밀번호 변경 시 예외가 발생한다")
        fun updatePasswordWithoutLogin() {
            whenever(authenticationService.getCurrentUser()).thenReturn(null)

            val exception = assertThrows(BusinessException::class.java) {
                userService.updatePassword(UserUpdatePasswordRequest("a", "b"))
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }
    }
}
