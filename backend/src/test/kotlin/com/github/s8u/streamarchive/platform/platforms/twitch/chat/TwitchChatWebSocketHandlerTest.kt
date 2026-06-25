package com.github.s8u.streamarchive.platform.platforms.twitch.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

class TwitchChatWebSocketHandlerTest {

    private val session = mockk<WebSocketSession>(relaxed = true)

    @Nested
    inner class HandleMessage {

        @Test
        fun `IRC 이모트 태그를 공통 이모지 DTO로 변환한다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = twitchChatWebSocketHandler(messages)
            val line = "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;" +
                "emotes=25:0-4,12-16/1902:6-10;id=message-1;mod=0;room-id=1337;" +
                "subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod " +
                ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa Keepo Kappa"

            handler.handleMessage(session, TextMessage(line))

            val actual = messages.single()
            assertEquals("ronni", actual.username)
            assertEquals("Kappa Keepo Kappa", actual.message)
            assertEquals(2, actual.emojis.size)
            assertEquals("Kappa", actual.emojis[0].placeholder)
            assertEquals(
                "https://static-cdn.jtvnw.net/emoticons/v2/25/default/dark/2.0",
                actual.emojis[0].imageUrl
            )
            assertEquals("Keepo", actual.emojis[1].placeholder)
            assertEquals(
                "https://static-cdn.jtvnw.net/emoticons/v2/1902/default/dark/2.0",
                actual.emojis[1].imageUrl
            )
        }

        @Test
        fun `서로게이트 이모지가 앞에 있어도 코드포인트 기준으로 이모트를 자른다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = twitchChatWebSocketHandler(messages)
            // 🦊는 코드포인트 1개지만 UTF-16에선 2 char를 차지한다
            // 트위치는 "Kappa"를 코드포인트 2-6으로 표기한다
            val line = "@display-name=ronni;emotes=25:2-6;id=message-1 " +
                ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :🦊 Kappa"

            handler.handleMessage(session, TextMessage(line))

            val actual = messages.single()
            assertEquals(1, actual.emojis.size)
            assertEquals("Kappa", actual.emojis[0].placeholder)
            assertEquals(
                "https://static-cdn.jtvnw.net/emoticons/v2/25/default/dark/2.0",
                actual.emojis[0].imageUrl
            )
        }

        @Test
        fun `IRC 이모트 태그가 없으면 이모지 목록을 비운다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = twitchChatWebSocketHandler(messages)
            val line = "@badge-info=;badges=;display-name=test-user;emotes=;id=message-1 " +
                ":test-user!test-user@test-user.tmi.twitch.tv PRIVMSG #test-channel :일반 채팅"

            handler.handleMessage(session, TextMessage(line))

            val actual = messages.single()
            assertEquals("일반 채팅", actual.message)
            assertEquals(emptyList(), actual.emojis)
        }

    }

    private fun twitchChatWebSocketHandler(
        messages: MutableList<PlatformChatMessageDto>
    ): TwitchChatWebSocketHandler {
        return TwitchChatWebSocketHandler(
            recordId = RECORD_ID,
            videoId = VIDEO_ID,
            platformChannelId = "test-channel",
            recordStartedAt = LocalDateTime.now().minusSeconds(1),
            onChat = { messages.add(it) },
            onConnectionClosed = {}
        )
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 2L
    }

}
