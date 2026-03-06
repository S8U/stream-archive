package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import com.github.s8u.streamarchive.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

@ExtendWith(MockitoExtension::class)
class AuthenticationServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var authenticationService: AuthenticationService

    private fun setAuthentication(username: String, role: String = "ROLE_USER") {
        val authorities = listOf(SimpleGrantedAuthority(role))
        val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("getCurrentUser")
    inner class GetCurrentUser {

        @Test
        @DisplayName("인증된 사용자를 반환한다")
        fun getAuthenticatedUser() {
            val user = User(
                id = 1L, uuid = "uuid", username = "testuser",
                name = "Test", password = "pass", role = Role.USER
            )
            setAuthentication("testuser")
            whenever(userRepository.findByUsername("testuser")).thenReturn(user)

            val result = authenticationService.getCurrentUser()

            assertNotNull(result)
            assertEquals("testuser", result?.username)
            clearAuthentication()
        }

        @Test
        @DisplayName("인증되지 않은 상태에서 null을 반환한다")
        fun getNullWhenNotAuthenticated() {
            clearAuthentication()

            val result = authenticationService.getCurrentUser()

            assertNull(result)
        }

        @Test
        @DisplayName("anonymousUser인 경우 null을 반환한다")
        fun getNullForAnonymousUser() {
            val auth = UsernamePasswordAuthenticationToken("anonymousUser", null)
            SecurityContextHolder.getContext().authentication = auth

            val result = authenticationService.getCurrentUser()

            assertNull(result)
            clearAuthentication()
        }
    }

    @Nested
    @DisplayName("getCurrentUserId")
    inner class GetCurrentUserId {

        @Test
        @DisplayName("인증된 사용자의 ID를 반환한다")
        fun getAuthenticatedUserId() {
            val user = User(
                id = 42L, uuid = "uuid", username = "testuser",
                name = "Test", password = "pass", role = Role.USER
            )
            setAuthentication("testuser")
            whenever(userRepository.findByUsername("testuser")).thenReturn(user)

            val result = authenticationService.getCurrentUserId()

            assertEquals(42L, result)
            clearAuthentication()
        }

        @Test
        @DisplayName("인증되지 않은 상태에서 null을 반환한다")
        fun getNullWhenNotAuthenticated() {
            clearAuthentication()

            val result = authenticationService.getCurrentUserId()

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("isAuthenticated")
    inner class IsAuthenticated {

        @Test
        @DisplayName("인증된 사용자는 true를 반환한다")
        fun authenticatedUser() {
            val user = User(
                id = 1L, uuid = "uuid", username = "testuser",
                name = "Test", password = "pass", role = Role.USER
            )
            setAuthentication("testuser")
            whenever(userRepository.findByUsername("testuser")).thenReturn(user)

            assertTrue(authenticationService.isAuthenticated())
            clearAuthentication()
        }

        @Test
        @DisplayName("인증되지 않은 상태에서 false를 반환한다")
        fun notAuthenticated() {
            clearAuthentication()

            assertFalse(authenticationService.isAuthenticated())
        }
    }

    @Nested
    @DisplayName("isAdmin")
    inner class IsAdmin {

        @Test
        @DisplayName("ADMIN 역할을 가진 사용자는 true를 반환한다")
        fun adminUser() {
            setAuthentication("admin", "ROLE_ADMIN")

            assertTrue(authenticationService.isAdmin())
            clearAuthentication()
        }

        @Test
        @DisplayName("USER 역할을 가진 사용자는 false를 반환한다")
        fun regularUser() {
            setAuthentication("user", "ROLE_USER")

            assertFalse(authenticationService.isAdmin())
            clearAuthentication()
        }

        @Test
        @DisplayName("인증되지 않은 상태에서 false를 반환한다")
        fun notAuthenticated() {
            clearAuthentication()

            assertFalse(authenticationService.isAdmin())
        }
    }
}
