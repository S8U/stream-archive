package com.github.s8u.streamarchive.platform.platforms.soop.service

import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopApiClient
import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopChatEmoticonDto
import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopChatEmoticonManifestDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class SoopChatEmoticonResolveServiceTest {

    private val apiClient = mockk<SoopApiClient>()
    private val soopChatEmoticonResolveService = SoopChatEmoticonResolveService(apiClient)

    @Test
    fun `메시지의 SOOP 이모티콘 토큰을 공통 이모지 DTO로 변환한다`() {
        every { apiClient.getChatEmoticonManifest() } returns SoopChatEmoticonManifestDto(
            defaultEmoticons = listOf(
                SoopChatEmoticonDto(keyword = "/emote-a/", fileName = "sample-a.png"),
                SoopChatEmoticonDto(keyword = "/emote-c/", fileName = "sample-c.png")
            ),
            subscribeEmoticons = listOf(
                SoopChatEmoticonDto(keyword = "/emote-b/", fileName = "sample/b.webp")
            )
        )

        val actual = soopChatEmoticonResolveService.resolve(
            "/emote-a//emote-a/ /emote-b/ /emote-c//emote-c//emote-c/ plain-text"
        )

        assertEquals(3, actual.size)
        assertEquals("/emote-a/", actual[0].placeholder)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-a.png", actual[0].imageUrl)
        assertEquals("/emote-b/", actual[1].placeholder)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample/b.webp", actual[1].imageUrl)
        assertEquals("/emote-c/", actual[2].placeholder)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-c.png", actual[2].imageUrl)
    }

    @Test
    fun `중단된 이모티콘과 파일명 없는 이모티콘은 제외한다`() {
        every { apiClient.getChatEmoticonManifest() } returns SoopChatEmoticonManifestDto(
            defaultEmoticons = listOf(
                SoopChatEmoticonDto(keyword = "/old/", fileName = "old.png", isDeprecated = true),
                SoopChatEmoticonDto(keyword = "/empty/", fileName = null)
            )
        )

        val actual = soopChatEmoticonResolveService.resolve("/old/ /empty/")

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `슬래시가 없으면 매니페스트를 조회하지 않는다`() {
        val actual = soopChatEmoticonResolveService.resolve("이모티콘 없는 일반 채팅")

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `캐시 TTL 안에서는 매니페스트를 다시 조회하지 않는다`() {
        val clock = TestClock(Instant.parse("2026-01-01T00:00:00Z"))
        val service = SoopChatEmoticonResolveService(apiClient, clock)
        every { apiClient.getChatEmoticonManifest() } returnsMany listOf(
            createManifest("sample-a.png"),
            createManifest("sample-b.png")
        )

        val first = service.resolve("/emote-a/")
        clock.plus(Duration.ofMinutes(59))
        val second = service.resolve("/emote-a/")

        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-a.png", first[0].imageUrl)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-a.png", second[0].imageUrl)
        verify(exactly = 1) { apiClient.getChatEmoticonManifest() }
    }

    @Test
    fun `캐시 TTL이 지나면 매니페스트를 다시 조회한다`() {
        val clock = TestClock(Instant.parse("2026-01-01T00:00:00Z"))
        val service = SoopChatEmoticonResolveService(apiClient, clock)
        every { apiClient.getChatEmoticonManifest() } returnsMany listOf(
            createManifest("sample-a.png"),
            createManifest("sample-b.png")
        )

        val first = service.resolve("/emote-a/")
        clock.plus(Duration.ofHours(1).plusSeconds(1))
        val second = service.resolve("/emote-a/")

        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-a.png", first[0].imageUrl)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample-b.png", second[0].imageUrl)
        verify(exactly = 2) { apiClient.getChatEmoticonManifest() }
    }

    private fun createManifest(fileName: String): SoopChatEmoticonManifestDto {
        return SoopChatEmoticonManifestDto(
            defaultEmoticons = listOf(
                SoopChatEmoticonDto(keyword = "/emote-a/", fileName = fileName)
            )
        )
    }

    private class TestClock(
        private var instant: Instant
    ) : Clock() {

        override fun getZone(): ZoneId {
            return ZoneOffset.UTC
        }

        override fun withZone(zone: ZoneId): Clock {
            return TestClock(instant)
        }

        override fun instant(): Instant {
            return instant
        }

        fun plus(duration: Duration) {
            instant = instant.plus(duration)
        }

    }

}
