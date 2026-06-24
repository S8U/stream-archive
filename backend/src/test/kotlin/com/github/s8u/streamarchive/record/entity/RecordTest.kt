package com.github.s8u.streamarchive.record.entity

import com.github.s8u.streamarchive.platform.enums.PlatformType
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordTest {

    private fun record(): Record {
        return Record(
            channelId = 1L,
            videoId = 1L,
            platformType = PlatformType.CHZZK,
            platformStreamId = "stream-1",
            recordQuality = "best"
        )
    }

    @Nested
    inner class End {

        @Test
        fun `녹화를 종료하면 isEnded가 true가 되고 종료 일시가 채워진다`() {
            val record = record()

            record.end(isCancelled = false)

            assertTrue(record.isEnded)
            assertNotNull(record.endedAt)
        }

        @Test
        fun `수동 취소로 종료하면 isCancelled가 true가 된다`() {
            val record = record()

            record.end(isCancelled = true)

            assertTrue(record.isEnded)
            assertTrue(record.isCancelled)
            assertNotNull(record.endedAt)
        }

        @Test
        fun `취소 아님으로 종료하면 isCancelled가 false로 유지된다`() {
            val record = record()

            record.end(isCancelled = false)

            assertFalse(record.isCancelled)
        }
    }

    @Nested
    inner class FailToStart {

        @Test
        fun `시작에 실패하면 isEnded와 isFailed가 true가 되고 종료 일시가 채워진다`() {
            val record = record()

            record.failToStart()

            assertTrue(record.isEnded)
            assertTrue(record.isFailed)
            assertNotNull(record.endedAt)
        }
    }

    @Nested
    inner class MarkFailed {

        @Test
        fun `실패로 표시하면 isFailed만 true가 되고 종료 상태는 바뀌지 않는다`() {
            val record = record()

            record.markFailed()

            assertTrue(record.isFailed)
            assertFalse(record.isEnded)
            assertNull(record.endedAt)
        }
    }

    @Nested
    inner class InitialState {

        @Test
        fun `생성 직후에는 종료·취소·실패가 모두 false다`() {
            val record = record()

            assertFalse(record.isEnded)
            assertFalse(record.isCancelled)
            assertFalse(record.isFailed)
            assertNull(record.endedAt)
        }
    }

}
