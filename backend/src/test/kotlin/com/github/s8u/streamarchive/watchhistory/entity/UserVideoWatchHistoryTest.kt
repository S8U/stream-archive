package com.github.s8u.streamarchive.watchhistory.entity

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserVideoWatchHistoryTest {

    private fun watchHistory(): UserVideoWatchHistory {
        return UserVideoWatchHistory(
            userId = 1L,
            videoId = 1L,
            lastPosition = 0
        )
    }

    @Nested
    inner class UpdatePosition {

        @Test
        fun `재생 위치를 갱신하면 lastPosition이 바뀐다`() {
            val watchHistory = watchHistory()

            watchHistory.updatePosition(120)

            assertEquals(120, watchHistory.lastPosition)
        }

        @Test
        fun `재생 위치를 갱신한 뒤에도 시청 일시는 채워져 있다`() {
            val watchHistory = watchHistory()

            watchHistory.updatePosition(120)

            assertNotNull(watchHistory.watchedAt)
        }
    }
}
