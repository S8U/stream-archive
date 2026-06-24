package com.github.s8u.streamarchive.platform.platforms.youtube.service

import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveStreamingDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItem
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemContentDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideoSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideosResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YoutubeLiveFindServiceTest {

    private val apiClient = mockk<YoutubeApiClient>()
    private val liveFindService = YoutubeLiveFindService(apiClient)

    @Nested
    inner class Find {

        @Test
        fun `업로드 재생목록에서 라이브 동영상을 찾는다`() {
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(liveVideo(VIDEO_ID))
            )

            val result = liveFindService.find(CHANNEL_ID)

            assertEquals(CHANNEL_ID, result?.channelId)
            assertEquals(VIDEO_ID, result?.video?.id)
        }

        @Test
        fun `라이브가 없으면 null을 반환한다`() {
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(endedVideo(VIDEO_ID))
            )

            val result = liveFindService.find(CHANNEL_ID)

            assertNull(result)
        }

        @Test
        fun `두 번째 조회는 캐싱된 동영상으로 재생목록 호출을 건너뛴다`() {
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returns YoutubeVideosResponse(
                items = listOf(liveVideo(VIDEO_ID))
            )

            liveFindService.find(CHANNEL_ID)
            liveFindService.find(CHANNEL_ID)

            // 재생목록은 첫 조회 한 번만 부르고, 두 번째는 캐싱된 동영상만 확인한다
            verify(exactly = 1) { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) }
            verify(exactly = 2) { apiClient.getVideos(listOf(VIDEO_ID)) }
        }

        @Test
        fun `캐싱된 라이브가 끝나면 재생목록을 다시 조회한다`() {
            every { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) } returns YoutubePlaylistItemsResponse(
                items = listOf(playlistItem(VIDEO_ID))
            )
            // 첫 조회는 라이브, 두 번째 조회는 종료된 상태로 응답한다
            every { apiClient.getVideos(listOf(VIDEO_ID)) } returnsMany listOf(
                YoutubeVideosResponse(items = listOf(liveVideo(VIDEO_ID))),
                YoutubeVideosResponse(items = listOf(endedVideo(VIDEO_ID)))
            )

            liveFindService.find(CHANNEL_ID)
            val second = liveFindService.find(CHANNEL_ID)

            assertNull(second)
            // 캐싱이 무효화돼 재생목록을 다시 조회한다
            verify(exactly = 2) { apiClient.getPlaylistItems(UPLOADS_PLAYLIST_ID, any()) }
        }

    }

    private fun playlistItem(videoId: String): YoutubePlaylistItem {
        return YoutubePlaylistItem(
            contentDetails = YoutubePlaylistItemContentDetails(videoId = videoId)
        )
    }

    private fun liveVideo(videoId: String): YoutubeVideo {
        return YoutubeVideo(
            id = videoId,
            snippet = YoutubeVideoSnippet(liveBroadcastContent = "live"),
            liveStreamingDetails = YoutubeLiveStreamingDetails(
                actualStartTime = "2026-05-13T11:00:00Z"
            )
        )
    }

    private fun endedVideo(videoId: String): YoutubeVideo {
        return YoutubeVideo(
            id = videoId,
            snippet = YoutubeVideoSnippet(liveBroadcastContent = "none"),
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
