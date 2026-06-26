package com.github.s8u.streamarchive.platform.platforms.soop.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatEmojiDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.platforms.soop.service.SoopChatEmoticonResolveService
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

class SoopChatWebSocketHandlerTest {

    private val session = mockk<WebSocketSession>(relaxed = true)
    private val soopChatEmoticonResolveService = mockk<SoopChatEmoticonResolveService>()

    @Test
    fun `SOOP 일반 채팅 패킷을 공통 채팅 DTO로 변환한다`() {
        every { soopChatEmoticonResolveService.resolve(any()) } returns emptyList()
        val messages = mutableListOf<PlatformChatMessageDto>()
        val handler = soopChatWebSocketHandler(messages)
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.CHAT_MESSAGE,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                "테스트 채팅",
                SoopChatPacketUtils.FORM_FEED,
                "sample-user",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "sample-nickname",
                SoopChatPacketUtils.FORM_FEED
            )
        )

        handler.handleMessage(session, BinaryMessage(packet))

        val actual = messages.single()
        assertEquals("sample-nickname", actual.username)
        assertEquals("테스트 채팅", actual.message)
    }

    @Test
    fun `SOOP 기본 이모티콘 토큰을 공통 이모지 DTO로 변환한다`() {
        every { soopChatEmoticonResolveService.resolve("/sample-emote//sample-emote/") } returns listOf(
            PlatformChatEmojiDto(
                placeholder = "/sample-emote/",
                imageUrl = "https://res.sooplive.com/images/chat/emoticon/big/sample.png"
            )
        )
        val messages = mutableListOf<PlatformChatMessageDto>()
        val handler = soopChatWebSocketHandler(messages)
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.CHAT_MESSAGE,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                "/sample-emote//sample-emote/",
                SoopChatPacketUtils.FORM_FEED,
                "sample-user",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "sample-nickname",
                SoopChatPacketUtils.FORM_FEED
            )
        )

        handler.handleMessage(session, BinaryMessage(packet))

        val actual = messages.single()
        assertEquals("/sample-emote//sample-emote/", actual.message)
        assertEquals(1, actual.emojis.size)
        assertEquals("/sample-emote/", actual.emojis[0].placeholder)
        assertEquals("https://res.sooplive.com/images/chat/emoticon/big/sample.png", actual.emojis[0].imageUrl)
    }

    @Test
    fun `SOOP OGQ 이모티콘 패킷을 공통 채팅 DTO로 변환한다`() {
        every { soopChatEmoticonResolveService.resolve(any()) } returns emptyList()
        val messages = mutableListOf<PlatformChatMessageDto>()
        val handler = soopChatWebSocketHandler(messages)
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.OGQ_EMOTICON,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                "12345",
                SoopChatPacketUtils.FORM_FEED,
                "sample message",
                SoopChatPacketUtils.FORM_FEED,
                "ogq-sample",
                SoopChatPacketUtils.FORM_FEED,
                "4",
                SoopChatPacketUtils.FORM_FEED,
                "1",
                SoopChatPacketUtils.FORM_FEED,
                "sample-user",
                SoopChatPacketUtils.FORM_FEED,
                "sample-nickname",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "0",
                SoopChatPacketUtils.FORM_FEED,
                "png",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "1",
                SoopChatPacketUtils.FORM_FEED
            )
        )

        handler.handleMessage(session, BinaryMessage(packet))

        val actual = messages.single()
        assertEquals("sample-nickname", actual.username)
        assertEquals("{:soop-ogq-ogq-sample-4:} sample message", actual.message)
        assertEquals(1, actual.emojis.size)
        assertEquals("{:soop-ogq-ogq-sample-4:}", actual.emojis[0].placeholder)
        assertEquals(
            "https://ogq-sticker-global-cdn-z01.sooplive.co.kr/sticker/ogq-sample/4_80.webp",
            actual.emojis[0].imageUrl
        )
    }

    private fun soopChatWebSocketHandler(
        messages: MutableList<PlatformChatMessageDto>
    ): SoopChatWebSocketHandler {
        return SoopChatWebSocketHandler(
            chatRoomNo = "12345",
            fanTicket = "test-fan-ticket",
            soopChatEmoticonResolveService = soopChatEmoticonResolveService,
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
