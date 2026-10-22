package com.github.s8u.streamarchive.user.enums

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserSettingKeyTest {

    @Nested
    inner class IsAvailableTo {

        @Test
        fun `관리자 전용 설정은 관리자만 쓸 수 있다`() {
            val key = UserSettingKey.SHOW_UNLISTED_CHANNELS

            assertTrue(key.isAvailableTo(isAdmin = true))
            assertFalse(key.isAvailableTo(isAdmin = false))
        }
    }

    @Nested
    inner class ResolveValue {

        @Test
        fun `저장된 값이 없으면 기본값을 쓴다`() {
            val key = UserSettingKey.SHOW_UNLISTED_VIDEOS

            assertEquals(key.defaultValue, key.resolveValue(savedValue = null, isAdmin = true))
        }

        @Test
        fun `관리자는 저장된 값을 쓴다`() {
            val key = UserSettingKey.SHOW_UNLISTED_VIDEOS

            assertEquals("true", key.resolveValue(savedValue = "true", isAdmin = true))
        }

        @Test
        fun `관리자 전용 설정은 관리자가 아니면 저장된 값이 있어도 기본값을 쓴다`() {
            val key = UserSettingKey.SHOW_UNLISTED_CHANNELS

            assertEquals(key.defaultValue, key.resolveValue(savedValue = "true", isAdmin = false))
        }
    }

}
