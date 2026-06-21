package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.exception.BusinessException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

class VideoAutoDeletePolicyTest {

    @Nested
    inner class Create {

        @Test
        fun `삭제 기준 일수가 1일 미만이면 예외를 던진다`() {
            val exception = assertThrows<BusinessException> {
                VideoAutoDeletePolicy(isEnabled = true, deleteAfterDays = 0)
            }

            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        }

        @Test
        fun `유효한 값으로 정책을 생성한다`() {
            val policy = VideoAutoDeletePolicy(
                channelId = CHANNEL_ID,
                isEnabled = true,
                deleteAfterDays = 30
            )

            assertEquals(CHANNEL_ID, policy.channelId)
            assertTrue(policy.isEnabled)
            assertEquals(30, policy.deleteAfterDays)
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `전달한 값만 수정한다`() {
            val policy = VideoAutoDeletePolicy(isEnabled = false, deleteAfterDays = 30)

            policy.update(isEnabled = true, deleteAfterDays = null)

            assertTrue(policy.isEnabled)
            assertEquals(30, policy.deleteAfterDays)
        }

        @Test
        fun `모든 값이 null이면 기존 값을 유지한다`() {
            val policy = VideoAutoDeletePolicy(isEnabled = false, deleteAfterDays = 30)

            policy.update(isEnabled = null, deleteAfterDays = null)

            assertFalse(policy.isEnabled)
            assertEquals(30, policy.deleteAfterDays)
        }

        @Test
        fun `잘못된 삭제 기준 일수로 수정하면 기존 값을 유지한다`() {
            val policy = VideoAutoDeletePolicy(isEnabled = false, deleteAfterDays = 30)

            val exception = assertThrows<BusinessException> {
                policy.update(isEnabled = null, deleteAfterDays = 0)
            }

            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
            assertEquals(30, policy.deleteAfterDays)
        }
    }

    companion object {
        private const val CHANNEL_ID = 1L
    }

}
