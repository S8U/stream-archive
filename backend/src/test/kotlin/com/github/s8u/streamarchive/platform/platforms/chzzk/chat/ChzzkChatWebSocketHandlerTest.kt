package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

class ChzzkChatWebSocketHandlerTest {

    private val objectMapper = jacksonObjectMapper()
    private val session = mockk<WebSocketSession>(relaxed = true)

    @Nested
    inner class HandleMessage {

        @Test
        fun `채팅 명령의 메시지를 공통 채팅 DTO로 변환한다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(session, chatFrame(nickname = "시청자", userRoleCode = "common_user", msg = "안녕하세요"))

            val actual = messages.single()
            assertEquals("시청자", actual.username)
            assertEquals("안녕하세요", actual.message)
            assertNull(actual.userRole)
        }

        @ParameterizedTest
        @CsvSource(
            "streamer, STREAMER",
            "streaming_chat_manager, MANAGER",
            "streaming_channel_manager, MANAGER",
            "streaming_settlement_manager, MANAGER",
            "manager, MANAGER"
        )
        fun `작성자 역할 코드를 공통 역할로 변환한다`(userRoleCode: String, expected: PlatformChatUserRole) {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(session, chatFrame(nickname = "작성자", userRoleCode = userRoleCode, msg = "채팅"))

            assertEquals(expected, messages.single().userRole)
        }

        @Test
        fun `알 수 없는 역할 코드는 일반 시청자로 본다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(session, chatFrame(nickname = "작성자", userRoleCode = "new_role", msg = "채팅"))

            assertNull(messages.single().userRole)
        }

    }

    private fun chzzkChatWebSocketHandler(messages: MutableList<PlatformChatMessageDto>): ChzzkChatWebSocketHandler {
        return ChzzkChatWebSocketHandler(
            chatChannelId = "test-chat-channel",
            chatAccessToken = "test-access-token",
            recordId = RECORD_ID,
            videoId = VIDEO_ID,
            platformChannelId = "test-channel",
            recordStartedAt = LocalDateTime.now().minusSeconds(1),
            onChat = { messages.add(it) },
            onRaw = {},
            onConnectionClosed = {}
        )
    }

    private fun chatFrame(
        nickname: String,
        userRoleCode: String,
        msg: String
    ): TextMessage {
        // 치지직은 profile과 extras를 JSON 문자열로 한 번 더 감싸서 보낸다
        val profile = objectMapper.writeValueAsString(
            mapOf(
                "nickname" to nickname,
                "userRoleCode" to userRoleCode
            )
        )
        val extras = objectMapper.writeValueAsString(mapOf("emojis" to emptyMap<String, String>()))
        val frame = mapOf(
            "ver" to "3",
            "cmd" to ChzzkChatCommandType.CHAT.value,
            "bdy" to listOf(
                mapOf(
                    "msg" to msg,
                    "msgTypeCode" to 1,
                    "profile" to profile,
                    "extras" to extras
                )
            )
        )

        return TextMessage(objectMapper.writeValueAsString(frame))
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 2L
    }

}
