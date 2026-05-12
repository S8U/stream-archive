package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.client.chzzk.ChzzkApiClient
import com.github.s8u.streamarchive.client.chzzk.ChzzkChannelDto
import com.github.s8u.streamarchive.client.chzzk.ChzzkLiveDetailDto
import com.github.s8u.streamarchive.client.chzzk.ChzzkResponseDto
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.impl.ChzzkStrategy
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class ChzzkStrategyTest {

    private val apiClient = mockk<ChzzkApiClient>()
    private val chzzkStrategy = ChzzkStrategy(apiClient)

    @Test
    fun `스트림 URL을 생성하면 치지직 도메인을 포함한다`() {
        val streamUrl = chzzkStrategy.getStreamUrl(CHANNEL_ID)

        assertTrue(streamUrl.contains("chzzk.naver.com"))
        assertTrue(streamUrl.contains(CHANNEL_ID))
    }

    @Test
    fun `채널 조회 응답을 공통 채널 DTO로 변환한다`() {
        every { apiClient.getChannel(CHANNEL_ID) } returns ChzzkResponseDto(
            code = 200,
            message = null,
            content = chzzkChannel()
        )

        val channel = chzzkStrategy.getChannel(CHANNEL_ID)

        val actual = assertNotNull(channel)
        assertEquals(PlatformType.CHZZK, actual.platformType)
        assertEquals(CHANNEL_ID, actual.id)
        assertEquals(CHANNEL_ID, actual.username)
        assertEquals("테스트 채널", actual.name)
        assertEquals("https://example.com/profile.png", actual.thumbnailUrl)
    }

    @Test
    fun `채널 조회 응답이 비정상이면 null을 반환한다`() {
        every { apiClient.getChannel(CHANNEL_ID) } returns ChzzkResponseDto(
            code = 404,
            message = "Not Found",
            content = null
        )

        val channel = chzzkStrategy.getChannel(CHANNEL_ID)

        assertNull(channel)
    }

    @Test
    fun `OPEN 상태의 라이브 상세 응답을 공통 스트림 DTO로 변환한다`() {
        every { apiClient.getLiveDetail(CHANNEL_ID) } returns ChzzkResponseDto(
            code = 200,
            message = null,
            content = chzzkLiveDetail(status = "OPEN", liveId = LIVE_ID)
        )

        val stream = chzzkStrategy.getStream(CHANNEL_ID)

        val actual = assertNotNull(stream)
        assertEquals(PlatformType.CHZZK, actual.platformType)
        assertEquals(LIVE_ID.toString(), actual.id)
        assertEquals(CHANNEL_ID, actual.username)
        assertEquals("테스트 방송", actual.title)
        assertEquals("테스트 카테고리", actual.category)
        assertEquals(321, actual.viewerCount)
        assertEquals("https://example.com/720.jpg", actual.thumbnailUrl)
        assertEquals(2026, actual.startedAt?.year)
    }

    @Test
    fun `CLOSE 상태의 라이브 상세 응답은 null을 반환한다`() {
        every { apiClient.getLiveDetail(CHANNEL_ID) } returns ChzzkResponseDto(
            code = 200,
            message = null,
            content = chzzkLiveDetail(status = "CLOSE", liveId = LIVE_ID)
        )

        val stream = chzzkStrategy.getStream(CHANNEL_ID)

        assertNull(stream)
    }

    @Test
    fun `liveId가 없으면 null을 반환한다`() {
        every { apiClient.getLiveDetail(CHANNEL_ID) } returns ChzzkResponseDto(
            code = 200,
            message = null,
            content = chzzkLiveDetail(status = "OPEN", liveId = null)
        )

        val stream = chzzkStrategy.getStream(CHANNEL_ID)

        assertNull(stream)
    }

    private fun chzzkChannel(): ChzzkChannelDto {
        return ChzzkChannelDto(
            channelId = CHANNEL_ID,
            channelName = "테스트 채널",
            channelImageUrl = "https://example.com/profile.png",
            verifiedMark = true,
            channelType = "STREAMING",
            channelDescription = "테스트 채널 설명",
            followerCount = 1000,
            openLive = true,
            subscriptionAvailability = true
        )
    }

    private fun chzzkLiveDetail(status: String, liveId: Long?): ChzzkLiveDetailDto {
        return ChzzkLiveDetailDto(
            liveId = liveId,
            liveTitle = "테스트 방송",
            status = status,
            liveImageUrl = "https://example.com/{type}.jpg",
            defaultThumbnailImageUrl = null,
            concurrentUserCount = 321,
            accumulateCount = 1000,
            openDate = "2026-05-13 20:00:00",
            closeDate = null,
            adult = false,
            tags = listOf("test"),
            categoryType = "GAME",
            liveCategory = "test_game",
            liveCategoryValue = "테스트 카테고리",
            channel = chzzkChannel(),
            chatChannelId = "test-chat-channel",
            livePlaybackJson = "{}"
        )
    }

    companion object {
        private const val CHANNEL_ID = "test-channel-id"
        private const val LIVE_ID = 123L
    }
}
