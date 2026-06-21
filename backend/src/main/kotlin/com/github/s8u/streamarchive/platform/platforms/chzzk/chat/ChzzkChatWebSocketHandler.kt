package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.LocalDateTime

class ChzzkChatWebSocketHandler(
    private val chatChannelId: String,
    private val chatAccessToken: String,
    recordId: Long,
    videoId: Long,
    platformChannelId: String,
    recordStartedAt: LocalDateTime,
    onChat: (chatMessageDto: PlatformChatMessageDto) -> Unit,
    onConnectionClosed: () -> Unit
) : PlatformChatWebSocketHandler(
    recordId,
    videoId,
    platformChannelId,
    recordStartedAt,
    onChat,
    onConnectionClosed
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("ChzzkChatWebSocketHandler: Chzzk chat established (recordId: {})", recordId)

        // 채팅방 접속
        sendConnectPacket(session)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.debug("ChzzkChatWebSocketHandler: Chzzk chat handle message (recordId: {}, payload: {})", recordId, message.payload.toString())

        val json = message.payload.toString()
        val root = objectMapper.readTree(json)

        val cmd = root.path("cmd").asInt()
        when (cmd) {
            // PING PONG
            ChzzkChatCommandType.PING.value -> {
                sendPongPacket(session)
                return
            }

            // 채팅
            ChzzkChatCommandType.CHAT.value -> {
                val bdy = root.path("bdy")
                if (bdy.isArray) {
                    for (node in bdy) {
                        val profileJson = node.path("profile").asText()
                        val profile = objectMapper.readTree(profileJson)

                        val nickname = profile.path("nickname").asText()
                        val msg = node.path("msg").asText()
                        val msgTime = node.path("msgTime").asLong()

                        val time = LocalDateTime.now()
                        val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

                        val chatMessageDto = PlatformChatMessageDto(
                            recordId = recordId,
                            videoId = videoId,
                            username = nickname,
                            message = msg,
                            offsetMillis = offsetMillis,
                            createdAt = time
                        )
                        onChat(chatMessageDto)
                    }
                }
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(
            "ChzzkChatWebSocketHandler: Chzzk chat transport error (recordId: {}, sessionId: {})",
            recordId,
            session.id,
            exception
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        if (closeStatus == CloseStatus.NORMAL) {
            logger.info(
                "ChzzkChatWebSocketHandler: Chzzk chat closed (recordId: {}, sessionId: {}, code: {}, reason: {})",
                recordId,
                session.id,
                closeStatus.code,
                closeStatus.reason
            )
        } else {
            logger.warn(
                "ChzzkChatWebSocketHandler: Chzzk chat closed abnormally (recordId: {}, sessionId: {}, code: {}, reason: {})",
                recordId,
                session.id,
                closeStatus.code,
                closeStatus.reason
            )
        }

        onConnectionClosed()
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    private fun sendConnectPacket(session: WebSocketSession) {
        val packet = mapOf(
            "ver" to "3",
            "cmd" to ChzzkChatCommandType.REQUEST_CONNECT.value,
            "svcid" to "game",
            "cid" to chatChannelId,
            "tid" to 1,
            "bdy" to mapOf(
                "uid" to null,
                "devType" to 2001,
                "accTkn" to chatAccessToken,
                "auth" to "READ",
                "libVer" to "4.9.3",
                "osVer" to "Windows/10",
                "devName" to "Google Chrome/140.0.0.0",
                "locale" to "ko",
                "timezone" to "Asia/Seoul"
            ),
        )

        val json = objectMapper.writeValueAsString(packet)
        sendMessage(session, json)

        logger.debug("ChzzkChatWebSocketHandler: Chzzk chat channel connect packet sent (recordId: {})", recordId)
    }

    private fun sendPongPacket(session: WebSocketSession) {
        val packet = mapOf(
            "ver" to 3,
            "cmd" to ChzzkChatCommandType.PONG.value
        )

        val json = objectMapper.writeValueAsString(packet)
        sendMessage(session, json)

        logger.debug("ChzzkChatWebSocketHandler: Chzzk chat pong packet sent (recordId: {})", recordId)
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        session.sendMessage(TextMessage(message))
    }

}
