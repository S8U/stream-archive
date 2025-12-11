package com.github.s8u.streamarchive.chat.twitch

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.chat.ChatWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * 트위치 IRC WebSocket 채팅 핸들러
 *
 * 트위치 IRC 프로토콜을 사용하여 채팅을 수집합니다.
 * 익명 접속(justinfan)을 사용하여 OAuth 토큰 없이 읽기 전용으로 연결합니다.
 */
class TwitchChatWebSocketHandler(
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

    private val logger = LoggerFactory.getLogger(TwitchChatWebSocketHandler::class.java)

    // 익명 접속용 justinfan 닉네임 (난수 포함)
    private val anonymousNick = "justinfan${Random.nextInt(10000, 99999)}"

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Twitch chat established (recordId: {})", recordId)

        // IRCv3 capabilities 요청 (태그 정보 포함)
        sendMessage(session, "CAP REQ :twitch.tv/tags twitch.tv/commands")

        // 익명 닉네임으로 인증 (PASS 없이 NICK만 전송)
        sendMessage(session, "NICK $anonymousNick")

        // 채널 입장
        sendMessage(session, "JOIN #$platformChannelId")

        logger.debug("Twitch chat channel joined (recordId: {}, channel: {})", recordId, platformChannelId)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val rawMessage = message.payload.toString()
        logger.debug("Twitch chat handle message (recordId: {}, payload: {})", recordId, rawMessage)

        // 여러 줄의 메시지가 올 수 있음
        rawMessage.lines().forEach { line ->
            if (line.isBlank()) return@forEach

            when {
                // PING 처리 - 서버 연결 유지
                line.startsWith("PING") -> {
                    sendMessage(session, "PONG :tmi.twitch.tv")
                    logger.debug("Twitch chat pong sent (recordId: {})", recordId)
                }

                // PRIVMSG 처리 - 실제 채팅 메시지
                line.contains("PRIVMSG") -> {
                    parseChatMessage(line)?.let { chatMessage ->
                        onChat(chatMessage)
                    }
                }
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.debug("Twitch chat error (recordId: {})", recordId, exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("Twitch chat closed (recordId: {})", recordId)
        onConnectionClosed()
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    /**
     * IRC 메시지를 파싱하여 ChatMessageDto로 변환
     *
     * IRC PRIVMSG 형식:
     * @badge-info=...;display-name=Username;... :username!username@username.tmi.twitch.tv PRIVMSG #channel :message
     */
    private fun parseChatMessage(line: String): ChatMessageDto? {
        try {
            // PRIVMSG 위치 찾기
            val privmsgIndex = line.indexOf("PRIVMSG")
            if (privmsgIndex == -1) return null

            // 태그 부분 파싱 (@ 로 시작하는 부분)
            val tags = mutableMapOf<String, String>()
            if (line.startsWith("@")) {
                val tagsEnd = line.indexOf(" ")
                if (tagsEnd > 0) {
                    val tagsString = line.substring(1, tagsEnd)
                    tagsString.split(";").forEach { tag ->
                        val parts = tag.split("=", limit = 2)
                        if (parts.size == 2) {
                            tags[parts[0]] = parts[1]
                        }
                    }
                }
            }

            // display-name 추출 (없으면 username 사용)
            val displayName = tags["display-name"]?.ifEmpty { null }

            // username 추출 (display-name이 없을 경우 fallback)
            val username = displayName ?: run {
                val userStart = line.indexOf(":") + 1
                val userEnd = line.indexOf("!")
                if (userStart > 0 && userEnd > userStart) {
                    line.substring(userStart, userEnd)
                } else {
                    "unknown"
                }
            }

            // 메시지 추출 (PRIVMSG #channel : 이후)
            val messageStart = line.indexOf(":", privmsgIndex)
            if (messageStart == -1) return null
            val chatText = line.substring(messageStart + 1)

            val time = LocalDateTime.now()
            val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

            return ChatMessageDto(
                recordId = recordId,
                videoId = videoId,
                username = username,
                message = chatText,
                offsetMillis = offsetMillis,
                createdAt = time
            )
        } catch (e: Exception) {
            logger.warn("Failed to parse Twitch chat message (recordId: {}): {}", recordId, line, e)
            return null
        }
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        session.sendMessage(TextMessage(message))
    }

}
