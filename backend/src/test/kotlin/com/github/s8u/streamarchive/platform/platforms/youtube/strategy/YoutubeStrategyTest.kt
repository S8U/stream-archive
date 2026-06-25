package com.github.s8u.streamarchive.platform.platforms.youtube.strategy

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveStreamingDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemContentDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeThumbnail
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideoSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideosResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeChannelFindService
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeLiveFindService
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YoutubeStrategyTest {

    private val apiClient = mockk<YoutubeApiClient>()
    private val channelFindService = YoutubeChannelFindService(apiClient)
    private val liveFindService = YoutubeLiveFindService(apiClient)
    private val youtubeStrategy = YoutubeStrategy(channelFindService, liveFindService)

    @Nested
    inner class GetStreamUrl {

        @Test
        fun `채널 ID 스트림 URL을 생성하면 유튜브 라이브 URL을 반환한다`() {
            val streamUrl = youtubeStrategy.getStreamUrl(CHANNEL_ID)

            assertTrue(streamUrl.contains("youtube.com/channel/$CHANNEL_ID/live"))
        }

        @Test
        fun `핸들 스트림 URL을 생성하면 유튜브 라이브 URL을 반환한다`() {
            val streamUrl = youtubeStrategy.getStreamUrl("@test-handle")

            assertTrue(streamUrl.contains("youtube.com/@test-handle/live"))
        }

    }

    @Nested
    inner class ParseChannelId {

        @Test
        fun `채널 URL에서 채널 ID를 추출한다`() {
            val channelId = youtubeStrategy.parseChannelId("https://www.youtube.com/channel/$CHANNEL_ID")

            assertEquals(CHANNEL_ID, channelId)
        }

        @Test
        fun `핸들 URL에서 핸들을 추출한다`() {
            val channelId = youtubeStrategy.parseChannelId("https://www.youtube.com/@test-handle")

            assertEquals("@test-handle", channelId)
        }

        @Test
        fun `유튜브 URL이 아니면 채널 ID를 추출하지 않는다`() {
            val channelId = youtubeStrategy.parseChannelId("https://www.twitch.tv/test-login")

            assertNull(channelId)
        }

    }

    @Nested
    inner class GetChannel {

        @Test
        fun `채널 응답을 공통 채널 DTO로 변환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(youtubeChannel())
            )

            val channel = youtubeStrategy.getChannel(CHANNEL_ID)

            val actual = assertNotNull(channel)
            assertEquals(PlatformType.YOUTUBE, actual.platformType)
            assertEquals(CHANNEL_ID, actual.id)
            assertEquals("@test-handle", actual.username)
            assertEquals("테스트 채널", actual.name)
            assertEquals("https://example.com/high.jpg", actual.thumbnailUrl)
        }

        @Test
        fun `핸들로 채널 응답을 조회한다`() {
            every { apiClient.getChannelByHandle("test-handle") } returns YoutubeChannelsResponse(
                items = listOf(youtubeChannel())
            )

            val channel = youtubeStrategy.getChannel("@test-handle")

            val actual = assertNotNull(channel)
            assertEquals(CHANNEL_ID, actual.id)
        }

        @Test
        fun `채널 응답이 비어 있으면 null을 반환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse()

            val channel = youtubeStrategy.getChannel(CHANNEL_ID)

            assertNull(channel)
        }

    }

    @Nested
    inner class GetStream {

        @Test
        fun `업로드 재생목록의 라이브 동영상을 공통 스트림 DTO로 변환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(youtubeChannel())
            )
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(liveVideo())
            )

            val stream = youtubeStrategy.getStream(CHANNEL_ID)

            val actual = assertNotNull(stream)
            assertEquals(PlatformType.YOUTUBE, actual.platformType)
            assertEquals(VIDEO_ID, actual.id)
            assertEquals(CHANNEL_ID, actual.username)
            assertEquals("테스트 방송", actual.title)
            assertEquals("20", actual.category)
            assertEquals(1234, actual.viewerCount)
            assertEquals("https://example.com/live-high.jpg", actual.thumbnailUrl)
            assertEquals(2026, actual.startedAt?.year)
        }

        @Test
        fun `라이브 동영상이 없으면 null을 반환한다`() {
            every { apiClient.getChannel(CHANNEL_ID) } returns YoutubeChannelsResponse(
                items = listOf(youtubeChannel())
            )
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(endedVideo())
            )

            val stream = youtubeStrategy.getStream(CHANNEL_ID)

            assertNull(stream)
        }

    }

    private fun youtubeChannel(): YoutubeChannelItem {
        return YoutubeChannelItem(
            id = CHANNEL_ID,
            snippet = YoutubeChannelSnippet(
                title = "테스트 채널",
                customUrl = "@test-handle",
                thumbnails = mapOf(
                    "default" to YoutubeThumbnail(url = "https://example.com/default.jpg"),
                    "high" to YoutubeThumbnail(url = "https://example.com/high.jpg")
                )
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
                channelId = CHANNEL_ID,
                channelTitle = "테스트 채널",
                title = "테스트 방송",
                categoryId = "20",
                publishedAt = "2026-05-13T10:55:00Z",
                liveBroadcastContent = "live",
                thumbnails = mapOf(
                    "high" to YoutubeThumbnail(url = "https://example.com/live-high.jpg")
                )
            ),
            liveStreamingDetails = YoutubeLiveStreamingDetails(
                actualStartTime = "2026-05-13T11:00:00Z",
                concurrentViewers = "1234"
            )
        )
    }

    private fun endedVideo(): YoutubeVideo {
        return YoutubeVideo(
            id = VIDEO_ID,
            snippet = YoutubeVideoSnippet(
                title = "끝난 방송",
                liveBroadcastContent = "none"
            ),
            liveStreamingDetails = YoutubeLiveStreamingDetails(
                actualStartTime = "2026-05-13T11:00:00Z",
                actualEndTime = "2026-05-13T13:00:00Z"
            )
        )
    }

    companion object {
        private const val CHANNEL_ID = "UC_test_channel_id_23456"
        private const val UPLOADS_PLAYLIST_ID = "UU_test_channel_id_23456"
        private const val VIDEO_ID = "video-123"
    }

}
