package com.github.s8u.streamarchive.platform.platforms.twitch.strategy

import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchApiClient
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchStreamResponseDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchStreamsRequestDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchStreamsResponseDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchUserResponseDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchUsersRequestDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchUsersResponseDto
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.twitch.strategy.TwitchStrategy
import com.github.s8u.streamarchive.platform.platforms.twitch.properties.TwitchProperties
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TwitchStrategyTest {

    private val apiClient = mockk<TwitchApiClient>()
    private val twitchStrategy = TwitchStrategy(
        apiClient = apiClient,
        twitchProperties = TwitchProperties(
            appClientId = "test-client-id",
            appClientSecret = "test-client-secret",
            personalOauthToken = ""
        )
    )

    @Test
    fun `스트림 URL을 생성하면 트위치 도메인을 포함한다`() {
        val streamUrl = twitchStrategy.getStreamUrl(LOGIN)

        assertTrue(streamUrl.contains("twitch.tv"))
        assertTrue(streamUrl.contains(LOGIN))
    }

    @Nested
    inner class ParseChannelId {

        @Test
        fun `채널 URL에서 로그인 아이디를 추출한다`() {
            val channelId = twitchStrategy.parseChannelId("https://www.twitch.tv/$URL_LOGIN")

            assertEquals(URL_LOGIN, channelId)
        }

        @Test
        fun `트위치 URL이 아니면 로그인 아이디를 추출하지 않는다`() {
            val channelId = twitchStrategy.parseChannelId(
                "https://chzzk.naver.com/live/abcdef0123456789abcdef0123456789"
            )

            assertNull(channelId)
        }

    }

    @Test
    fun `유저 응답을 공통 채널 DTO로 변환한다`() {
        every { apiClient.getUsers(TwitchUsersRequestDto(login = listOf(LOGIN))) } returns TwitchUsersResponseDto(
            data = listOf(twitchUser())
        )

        val channel = twitchStrategy.getChannel(LOGIN)

        val actual = assertNotNull(channel)
        assertEquals(PlatformType.TWITCH, actual.platformType)
        assertEquals(USER_ID, actual.id)
        assertEquals(LOGIN, actual.username)
        assertEquals("테스트 채널", actual.name)
        assertEquals("https://example.com/profile.png", actual.thumbnailUrl)
    }

    @Test
    fun `유저 응답이 비어 있으면 null을 반환한다`() {
        every { apiClient.getUsers(TwitchUsersRequestDto(login = listOf(LOGIN))) } returns TwitchUsersResponseDto(
            data = emptyList()
        )

        val channel = twitchStrategy.getChannel(LOGIN)

        assertNull(channel)
    }

    @Test
    fun `스트림 응답을 공통 스트림 DTO로 변환한다`() {
        every { apiClient.getStreams(TwitchStreamsRequestDto(userLogin = LOGIN)) } returns TwitchStreamsResponseDto(
            data = listOf(twitchStream())
        )

        val stream = twitchStrategy.getStream(LOGIN)

        val actual = assertNotNull(stream)
        assertEquals(PlatformType.TWITCH, actual.platformType)
        assertEquals(STREAM_ID, actual.id)
        assertEquals(LOGIN, actual.username)
        assertEquals("테스트 방송", actual.title)
        assertEquals("테스트태그1, 테스트태그2", actual.category)
        assertEquals(789, actual.viewerCount)
        assertEquals("https://example.com/1280x720.jpg", actual.thumbnailUrl)
        assertEquals(2026, actual.startedAt?.year)
    }

    @Test
    fun `스트림 응답이 비어 있으면 null을 반환한다`() {
        every { apiClient.getStreams(TwitchStreamsRequestDto(userLogin = LOGIN)) } returns TwitchStreamsResponseDto(
            data = emptyList()
        )

        val stream = twitchStrategy.getStream(LOGIN)

        assertNull(stream)
    }

    private fun twitchUser(): TwitchUserResponseDto {
        return TwitchUserResponseDto(
            id = USER_ID,
            login = LOGIN,
            displayName = "테스트 채널",
            profileImageUrl = "https://example.com/profile.png"
        )
    }

    private fun twitchStream(): TwitchStreamResponseDto {
        return TwitchStreamResponseDto(
            id = STREAM_ID,
            userId = USER_ID,
            userLogin = LOGIN,
            userName = "테스트 채널",
            gameId = "game-123",
            gameName = "테스트 게임",
            type = "live",
            title = "테스트 방송",
            tags = listOf("테스트태그1", "테스트태그2"),
            viewerCount = 789,
            startedAt = "2026-05-13T11:00:00Z",
            language = "ko",
            thumbnailUrl = "https://example.com/{width}x{height}.jpg",
            isMature = false
        )
    }

    companion object {
        private const val LOGIN = "test-login"
        private const val URL_LOGIN = "test_login"
        private const val USER_ID = "user-123"
        private const val STREAM_ID = "stream-123"
    }
}
