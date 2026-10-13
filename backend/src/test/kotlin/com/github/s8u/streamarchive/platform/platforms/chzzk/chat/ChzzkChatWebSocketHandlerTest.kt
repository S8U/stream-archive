package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
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
            assertEquals(PlatformChatMessageType.CHAT, actual.messageType)
            assertEquals("시청자", actual.username)
            assertEquals("안녕하세요", actual.message)
            assertNull(actual.userRole)
            assertNull(actual.donationAmount)
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

        @Test
        fun `후원 메시지를 금액과 함께 후원 채팅으로 변환한다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(
                session,
                donationFrame(
                    msgTypeCode = ChzzkChatMessageTypeCode.DONATION.value,
                    profile = mapOf("nickname" to "후원자", "userRoleCode" to "common_user"),
                    extras = mapOf(
                        "isAnonymous" to false,
                        "payAmount" to 50000,
                        "emojis" to emptyMap<String, String>()
                    ),
                    msg = "응원합니다"
                )
            )

            val actual = messages.single()
            assertEquals(PlatformChatMessageType.DONATION, actual.messageType)
            assertEquals("후원자", actual.username)
            assertEquals("응원합니다", actual.message)
            assertEquals(50000, actual.donationAmount)
        }

        @Test
        fun `익명 후원은 프로필이 없어도 고정 이름으로 변환한다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(
                session,
                donationFrame(
                    msgTypeCode = ChzzkChatMessageTypeCode.DONATION.value,
                    profile = null,
                    extras = mapOf("isAnonymous" to true, "payAmount" to 1000),
                    msg = "익명 후원"
                )
            )

            val actual = messages.single()
            assertEquals(PlatformChatMessageType.DONATION, actual.messageType)
            assertEquals("익명의 후원자", actual.username)
            assertNull(actual.userRole)
            assertEquals(1000, actual.donationAmount)
        }

        @Test
        fun `후원 명령으로 온 후원 외 메시지는 버린다`() {
            val messages = mutableListOf<PlatformChatMessageDto>()
            val handler = chzzkChatWebSocketHandler(messages)

            handler.handleMessage(
                session,
                donationFrame(
                    msgTypeCode = SUBSCRIPTION_MSG_TYPE_CODE,
                    profile = mapOf("nickname" to "구독자", "userRoleCode" to "common_user"),
                    extras = emptyMap(),
                    msg = "구독"
                )
            )

            assertEquals(emptyList(), messages)
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

    private fun donationFrame(
        msgTypeCode: Int,
        profile: Map<String, String>?,
        extras: Map<String, Any>,
        msg: String
    ): TextMessage {
        // 익명 후원은 profile이 문자열이 아니라 null로 온다
        val frame = mapOf(
            "ver" to "3",
            "cmd" to ChzzkChatCommandType.DONATION.value,
            "bdy" to listOf(
                mapOf(
                    "msg" to msg,
                    "msgTypeCode" to msgTypeCode,
                    "profile" to profile?.let { objectMapper.writeValueAsString(it) },
                    "extras" to objectMapper.writeValueAsString(extras)
                )
            )
        )

        return TextMessage(objectMapper.writeValueAsString(frame))
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 2L

        // 구독 알림의 msgTypeCode (우리 코드에서는 다루지 않는 값)
        private const val SUBSCRIPTION_MSG_TYPE_CODE = 11
    }

}
