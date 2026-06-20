package com.github.s8u.streamarchive.user.entity

import com.github.s8u.streamarchive.user.enums.Role
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTest {

    private fun user(): User {
        return User(
            uuid = "user-uuid",
            username = "tester",
            name = "원래 이름",
            password = "old-password",
            role = Role.USER
        )
    }

    @Nested
    inner class Update {

        @Test
        fun `name과 role을 넘기면 둘 다 갱신된다`() {
            val user = user()

            user.update(name = "새 이름", role = Role.ADMIN)

            assertEquals("새 이름", user.name)
            assertEquals(Role.ADMIN, user.role)
        }

        @Test
        fun `name만 넘기면 name만 바뀌고 role은 유지된다`() {
            val user = user()

            user.update(name = "새 이름", role = null)

            assertEquals("새 이름", user.name)
            assertEquals(Role.USER, user.role)
        }

        @Test
        fun `모든 인자가 null이면 아무 값도 바뀌지 않는다`() {
            val user = user()

            user.update(name = null, role = null)

            assertEquals("원래 이름", user.name)
            assertEquals(Role.USER, user.role)
        }
    }

    @Nested
    inner class UpdateName {

        @Test
        fun `이름을 수정하면 name이 갱신된다`() {
            val user = user()

            user.updateName("바뀐 이름")

            assertEquals("바뀐 이름", user.name)
        }
    }

    @Nested
    inner class ChangePassword {

        @Test
        fun `비밀번호를 변경하면 password가 갱신된다`() {
            val user = user()

            user.changePassword("encoded-new-password")

            assertEquals("encoded-new-password", user.password)
        }
    }

    @Nested
    inner class Login {

        @Test
        fun `로그인하면 마지막 로그인 일시가 채워진다`() {
            val user = user()

            user.login()

            assertNotNull(user.lastLoginAt)
        }
    }

    @Nested
    inner class InitialState {

        @Test
        fun `생성 직후에는 마지막 로그인 일시가 null이다`() {
            val user = user()

            assertNull(user.lastLoginAt)
        }
    }
}
