package com.github.s8u.streamarchive.platform.platforms.youtube.service

import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelsResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YoutubeChannelFindServiceTest {

    private val apiClient = mockk<YoutubeApiClient>()
    private val channelFindService = YoutubeChannelFindService(apiClient)

    @Nested
    inner class Find {

        @Test
        fun `채널 ID 형식이면 ID로 조회한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(channel())
            )

            val channel = channelFindService.find(CHANNEL_ID)

            assertEquals(CHANNEL_ID, channel?.id)
            verify { apiClient.getChannel(CHANNEL_ID) }
        }

        @Test
        fun `핸들 형식이면 핸들로 조회한다`() {
            every { apiClient.getChannelByHandle("test-handle") } returns YoutubeChannelsResponse(
                items = listOf(channel())
            )

            val channel = channelFindService.find("@test-handle")

            assertEquals(CHANNEL_ID, channel?.id)
            verify { apiClient.getChannelByHandle("test-handle") }
        }

        @Test
        fun `채널 ID 앞에 @가 붙어도 ID로 조회한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(channel())
            )

            channelFindService.find("@$CHANNEL_ID")

            verify { apiClient.getChannel(CHANNEL_ID) }
        }

        @Test
        fun `UC로 시작하지만 짧은 핸들은 핸들로 조회한다`() {
            every { apiClient.getChannelByHandle("UCLA") } returns YoutubeChannelsResponse(
                items = listOf(channel())
            )

            channelFindService.find("@UCLA")

            verify { apiClient.getChannelByHandle("UCLA") }
        }

        @Test
        fun `응답이 비어 있으면 null을 반환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse()

            val channel = channelFindService.find(CHANNEL_ID)

            assertNull(channel)
        }

    }

    @Nested
    inner class IsChannelId {

        @Test
        fun `24자 UC 형식이면 true를 반환한다`() {
            assertTrue(channelFindService.isChannelId(CHANNEL_ID))
        }

        @Test
        fun `UC로 시작해도 24자가 아니면 false를 반환한다`() {
            assertFalse(channelFindService.isChannelId("UCLA"))
        }

    }

    private fun channel(): YoutubeChannelItem {
        return YoutubeChannelItem(
            id = CHANNEL_ID,
            snippet = YoutubeChannelSnippet(title = "테스트 채널")
        )
    }

    companion object {
        private const val CHANNEL_ID = "UC_test_channel_id_23456"
    }

}
