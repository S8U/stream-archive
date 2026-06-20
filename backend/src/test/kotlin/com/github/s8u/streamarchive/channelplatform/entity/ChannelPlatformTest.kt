package com.github.s8u.streamarchive.channelplatform.entity

import com.github.s8u.streamarchive.platform.enums.PlatformType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChannelPlatformTest {

    private fun channelPlatform(): ChannelPlatform {
        return ChannelPlatform(
            platformType = PlatformType.CHZZK,
            platformChannelId = "origin-channel-id",
            isSyncProfile = true
        )
    }

    @Nested
    inner class Update {

        @Test
        fun `platformChannelId와 isSyncProfile을 넘기면 둘 다 갱신된다`() {
            val channelPlatform = channelPlatform()

            channelPlatform.update(platformChannelId = "new-channel-id", isSyncProfile = false)

            assertEquals("new-channel-id", channelPlatform.platformChannelId)
            assertFalse(channelPlatform.isSyncProfile)
        }

        @Test
        fun `platformChannelId만 넘기면 platformChannelId만 바뀌고 isSyncProfile은 유지된다`() {
            val channelPlatform = channelPlatform()

            channelPlatform.update(platformChannelId = "new-channel-id", isSyncProfile = null)

            assertEquals("new-channel-id", channelPlatform.platformChannelId)
            assertTrue(channelPlatform.isSyncProfile)
        }

        @Test
        fun `모든 인자가 null이면 아무 값도 바뀌지 않는다`() {
            val channelPlatform = channelPlatform()

            channelPlatform.update(platformChannelId = null, isSyncProfile = null)

            assertEquals("origin-channel-id", channelPlatform.platformChannelId)
            assertTrue(channelPlatform.isSyncProfile)
        }
    }
}
