package com.github.s8u.streamarchive.chat.chzzk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.chat.ChatWebSocketHandler
import com.github.s8u.streamarchive.client.chzzk.ChzzkApiClient
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ChzzkChatWebSocketHandler(
    val chzzkApiClient: ChzzkApiClient,
    recordId: Long,
    videoId: Long,
    platformChannelId: String,
    recordStartedAt: LocalDateTime,
    onChat: (chatMessageDto: ChatMessageDto) -> Unit,
    onConnectionClosed: () -> Unit
) : ChatWebSocketHandler(
    recordId,
    videoId,
    platformChannelId,
    recordStartedAt,
    onChat,
    onConnectionClosed
) {

    private val logger = LoggerFactory.getLogger(ChzzkChatWebSocketHandler::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    var chatChannelId: String? = null
    var chatAccessToken: String? = null
    var userId: String? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Chzzk chat established (recordId: {})", recordId)

        // 채팅 채널 ID
        val chzzkStreamDto = chzzkApiClient.getLiveDetail(platformChannelId)?.content
        chatChannelId = chzzkStreamDto?.chatChannelId

        if (chatChannelId == null) {
            logger.error("Chzzk chatChannelId is null (recordId: {})", recordId)
            return
        }

        // 채팅 Access Token
        val chatAccessTokenDto = chzzkApiClient.getChatAccessToken(chatChannelId!!)?.content
        chatAccessToken = chatAccessTokenDto?.accessToken

        if (chatAccessToken == null) {
            logger.error("Chzzk chatAccessToken is null (recordId: {})", recordId)
            return
        }

        // 채팅방 접속
        sendConnectPacket(session)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.debug("Chzzk chat handle message (recordId: {}, payload: {})", recordId, message.payload.toString())

        val json = message.payload.toString()
        val root = objectMapper.readTree(json)

        val cmd = root.path("cmd").asInt()
        when (cmd) {
            // PING PONG
            ChzzkChatCommand.PING.value -> {
                sendPongPacket(session)
                return
            }

            // 채팅
            ChzzkChatCommand.CHAT.value -> {
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

                        val chatMessageDto = ChatMessageDto(
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
        logger.debug("Chzzk chat error (recordId: {})", recordId)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("Chzzk chat closed (recordId: {})", recordId)
        onConnectionClosed()
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    private fun sendConnectPacket(session: WebSocketSession) {
        val packet = mapOf(
            "ver" to "3",
            "cmd" to ChzzkChatCommand.REQUEST_CONNECT.value,
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

        logger.debug("Chzzk chat channel connect packet sent (recordId: {})", recordId)
    }

    private fun sendPongPacket(session: WebSocketSession) {
        val packet = mapOf(
            "ver" to 3,
            "cmd" to ChzzkChatCommand.PONG.value
        )

        val json = objectMapper.writeValueAsString(packet)
        sendMessage(session, json)

        logger.debug("Chzzk chat pong packet sent (recordId: {})", recordId)
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        session.sendMessage(TextMessage(message))
    }

}