package com.github.s8u.streamarchive.platform.platforms.youtube.strategy

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessage
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageAuthorDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessagesResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatTextMessageDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveStreamingDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemContentDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideoSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideosResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeChannelFindService
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeLiveFindService
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YoutubeChatStrategyTest {

    private val apiClient = mockk<YoutubeApiClient>()
    private val channelFindService = YoutubeChannelFindService(apiClient)
    private val liveFindService = YoutubeLiveFindService(apiClient)
    private val youtubeChatStrategy = YoutubeChatStrategy(apiClient, channelFindService, liveFindService)

    @Nested
    inner class StartCollecting {

        @Test
        fun `라이브 채팅 메시지를 공통 채팅 DTO로 변환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(youtubeChannel())
            )
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(liveVideo())
            )
            every { apiClient.getLiveChatMessages(LIVE_CHAT_ID, null) } returns YoutubeLiveChatMessagesResponse(
                nextPageToken = "next-token",
                pollingIntervalMillis = 1000,
                offlineAt = "2026-05-13T11:01:00Z",
                items = listOf(youtubeChatMessage())
            )

            val latch = CountDownLatch(1)
            val messages = mutableListOf<PlatformChatMessageDto>()
            val session = youtubeChatStrategy.startCollecting(
                recordId = RECORD_ID,
                videoId = LOCAL_VIDEO_ID,
                platformChannelId = CHANNEL_ID,
                recordStartedAt = LocalDateTime.of(2026, 5, 13, 20, 0, 0),
                onChat = {
                    messages.add(it)
                    latch.countDown()
                },
                onClosed = {}
            )

            assertNotNull(session)
            assertTrue(latch.await(1, TimeUnit.SECONDS))

            val actual = messages.first()
            assertEquals(RECORD_ID, actual.recordId)
            assertEquals(LOCAL_VIDEO_ID, actual.videoId)
            assertEquals("테스트 사용자", actual.username)
            assertEquals("테스트 채팅", actual.message)
            assertEquals(5000, actual.offsetMillis)

            session.stop()
        }

    }

    @Nested
    inner class GetPlatformType {

        @Test
        fun `플랫폼 타입은 유튜브를 반환한다`() {
            assertEquals(PlatformType.YOUTUBE, youtubeChatStrategy.platformType)
        }

    }

    private fun youtubeChannel(): YoutubeChannelItem {
        return YoutubeChannelItem(
            id = CHANNEL_ID,
            snippet = YoutubeChannelSnippet(
                title = "테스트 채널"
            )
        )
    }

    private fun playlistItem(videoId: String): YoutubePlaylistItem {
        return YoutubePlaylistItem(
            contentDetails = YoutubePlaylistItemContentDetails(videoId = videoId)
        )
    }

    private fun liveVideo(): YoutubeVideo {
        return YoutubeVideo(
            id = VIDEO_ID,
            snippet = YoutubeVideoSnippet(
                title = "테스트 방송",
                liveBroadcastContent = "live"
            ),
            liveStreamingDetails = YoutubeLiveStreamingDetails(
                actualStartTime = "2026-05-13T11:00:00Z",
                activeLiveChatId = LIVE_CHAT_ID
            )
        )
    }

    private fun youtubeChatMessage(): YoutubeLiveChatMessage {
        return YoutubeLiveChatMessage(
            id = "chat-message-1",
            snippet = YoutubeLiveChatMessageSnippet(
                type = "textMessageEvent",
                publishedAt = "2026-05-13T11:00:05Z",
                textMessageDetails = YoutubeLiveChatTextMessageDetails(
                    messageText = "테스트 채팅"
                )
            ),
            authorDetails = YoutubeLiveChatMessageAuthorDetails(
                displayName = "테스트 사용자"
            )
        )
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val LOCAL_VIDEO_ID = 2L
        private const val CHANNEL_ID = "UC_test_channel_id_23456"
        private const val UPLOADS_PLAYLIST_ID = "UU_test_channel_id_23456"
        private const val VIDEO_ID = "video-123"
        private const val LIVE_CHAT_ID = "live-chat-123"
    }

}
