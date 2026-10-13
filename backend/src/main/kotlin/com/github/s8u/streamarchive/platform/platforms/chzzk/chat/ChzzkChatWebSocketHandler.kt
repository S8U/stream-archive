package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatEmojiDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
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
    onRaw: (rawMessageDto: PlatformChatRawMessageDto) -> Unit,
    onConnectionClosed: () -> Unit
) : PlatformChatWebSocketHandler(
    recordId,
    videoId,
    platformChannelId,
    recordStartedAt,
    onChat,
    onRaw,
    onConnectionClosed
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("ChzzkChatWebSocketHandler: Chzzk chat established (recordId: {})", recordId)

        // 채팅방 접속
        sendConnectPacket(session)
    }

    override fun handlePlatformMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.debug(
            "ChzzkChatWebSocketHandler: Chzzk chat handle message (recordId: {}, payload: {})",
            recordId,
            message.payload.toString()
        )

        val json = message.payload.toString()
        val root = objectMapper.readTree(json)

        val cmd = root.path("cmd").asInt()
        when (cmd) {
            // 핑퐁
            ChzzkChatCommandType.PING.value -> {
                sendPongPacket(session)
                return
            }

            // 채팅
            ChzzkChatCommandType.CHAT.value -> {
                val bdy = root.path("bdy")
                if (bdy.isArray) {
                    for (node in bdy) {
                        onChat(toChatMessage(node))
                    }
                }
            }

            // 후원
            // 같은 명령으로 구독 알림 등도 오지만 지금은 후원만 남긴다 (나머지는 채팅 원본에 남아 있다)
            ChzzkChatCommandType.DONATION.value -> {
                val bdy = root.path("bdy")
                if (bdy.isArray) {
                    for (node in bdy) {
                        if (node.path("msgTypeCode").asInt() != ChzzkChatMessageTypeCode.DONATION.value) continue
                        onChat(toDonationMessage(node))
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
                "ChzzkChatWebSocketHandler: Chzzk chat closed abnormally " +
                    "(recordId: {}, sessionId: {}, code: {}, reason: {})",
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

    private fun toChatMessage(node: JsonNode): PlatformChatMessageDto {
        val profile = readNestedJson(node, "profile")
        val msg = node.path("msg").asText()
        val time = LocalDateTime.now()

        return PlatformChatMessageDto(
            recordId = recordId,
            videoId = videoId,
            username = profile.path("nickname").asText(),
            userRole = getUserRole(profile),
            message = msg,
            emojis = getEmojis(node).filter { msg.contains(it.placeholder) },
            offsetMillis = Duration.between(recordStartedAt, time).toMillis(),
            createdAt = time
        )
    }

    /**
     * 후원 메시지를 공통 채팅 DTO로 바꾼다.
     *
     * 익명 후원은 프로필이 null로 와서 닉네임 대신 고정 이름을 쓰고 역할을 남기지 않는다.
     */
    private fun toDonationMessage(node: JsonNode): PlatformChatMessageDto {
        val profile = readNestedJson(node, "profile")
        val extras = readNestedJson(node, "extras")
        val msg = node.path("msg").asText()
        val time = LocalDateTime.now()

        val isAnonymous = extras.path("isAnonymous").asBoolean() || !profile.isObject

        return PlatformChatMessageDto(
            recordId = recordId,
            videoId = videoId,
            messageType = PlatformChatMessageType.DONATION,
            username = if (isAnonymous) ANONYMOUS_DONOR_NAME else profile.path("nickname").asText(),
            userRole = if (isAnonymous) null else getUserRole(profile),
            message = msg,
            donationAmount = extras.path("payAmount").asInt(),
            emojis = getEmojis(node).filter { msg.contains(it.placeholder) },
            offsetMillis = Duration.between(recordStartedAt, time).toMillis(),
            createdAt = time
        )
    }

    private fun getUserRole(profile: JsonNode): PlatformChatUserRole? {
        return ChzzkChatUserRoleCode.fromValue(profile.path("userRoleCode").asText())?.userRole
    }

    // 치지직은 profile과 extras를 JSON 문자열로 한 번 더 감싸서 보낸다
    private fun readNestedJson(
        node: JsonNode,
        fieldName: String
    ): JsonNode {
        val json = node.path(fieldName)
        if (!json.isTextual || json.asText().isBlank()) return MissingNode.getInstance()

        return objectMapper.readTree(json.asText())
    }

    private fun getEmojis(node: JsonNode): List<PlatformChatEmojiDto> {
        val extrasJson = node.path("extras").asText()
        if (extrasJson.isBlank()) return emptyList()

        return try {
            val extras = objectMapper.readTree(extrasJson)
            val emojisNode = extras.path("emojis")
            if (!emojisNode.isObject) return emptyList()

            val emojis = mutableListOf<PlatformChatEmojiDto>()
            for (field in emojisNode.properties()) {
                val imageUrl = field.value.asText()
                if (imageUrl.isBlank()) continue

                emojis.add(
                    PlatformChatEmojiDto(
                        placeholder = "{:${field.key}:}",
                        imageUrl = imageUrl
                    )
                )
            }

            emojis
        } catch (e: Exception) {
            logger.warn(
                "ChzzkChatWebSocketHandler: Failed to parse Chzzk chat emojis (recordId: {})",
                recordId,
                e
            )
            emptyList()
        }
    }

    companion object {
        private const val ANONYMOUS_DONOR_NAME = "익명의 후원자"
    }

}
