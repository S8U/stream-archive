package com.github.s8u.streamarchive.platform.platforms.twitch.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatEmojiDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
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
 * 익명 접속(justinfan)을 사용하여 OAuth 토큰 없이 읽기 전용으로 연결합니다.
 */
class TwitchChatWebSocketHandler(
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

    // 익명 접속용 justinfan 닉네임 (난수 포함)
    private val anonymousNick = "justinfan${Random.nextInt(10000, 99999)}"

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("TwitchChatWebSocketHandler: Twitch chat established (recordId: {})", recordId)

        // IRCv3 capabilities 요청 (태그 정보 포함)
        sendMessage(session, "CAP REQ :twitch.tv/tags twitch.tv/commands")

        // 익명 닉네임으로 인증 (PASS 없이 NICK만 전송)
        sendMessage(session, "NICK $anonymousNick")

        // 채널 입장
        sendMessage(session, "JOIN #$platformChannelId")

        logger.debug("TwitchChatWebSocketHandler: Twitch chat channel joined (recordId: {}, channel: {})", recordId, platformChannelId)
    }

    override fun handlePlatformMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val rawMessage = message.payload.toString()
        logger.debug("TwitchChatWebSocketHandler: Twitch chat handle message (recordId: {}, payload: {})", recordId, rawMessage)

        // 여러 줄의 메시지가 올 수 있음
        rawMessage.lines().forEach { line ->
            if (line.isBlank()) return@forEach

            when {
                // PING 처리 (서버 연결 유지)
                line.startsWith("PING") -> {
                    sendMessage(session, "PONG :tmi.twitch.tv")
                    logger.debug("TwitchChatWebSocketHandler: Twitch chat pong sent (recordId: {})", recordId)
                }

                // PRIVMSG 처리 (실제 채팅 메시지)
                line.contains("PRIVMSG") -> {
                    parseChatMessage(line)?.let { chatMessage ->
                        onChat(chatMessage)
                    }
                }
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.debug("TwitchChatWebSocketHandler: Twitch chat error (recordId: {})", recordId, exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("TwitchChatWebSocketHandler: Twitch chat closed (recordId: {})", recordId)
        onConnectionClosed()
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    /**
     * IRC 메시지를 파싱하여 PlatformChatMessageDto로 변환
     *
     * IRC PRIVMSG 형식:
     * @badge-info=...;display-name=Username;... :username!username@username.tmi.twitch.tv PRIVMSG #channel :message
     */
    private fun parseChatMessage(line: String): PlatformChatMessageDto? {
        try {
            // PRIVMSG 위치 찾기
            val privmsgIndex = line.indexOf("PRIVMSG")
            if (privmsgIndex == -1) return null

            // 태그 부분 파싱 (@ 로 시작하는 부분)
            val tags = getTags(line)

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
            val emojis = getEmojis(tags, chatText)

            val time = LocalDateTime.now()
            val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

            return PlatformChatMessageDto(
                recordId = recordId,
                videoId = videoId,
                username = username,
                userRole = getUserRole(tags),
                message = chatText,
                emojis = emojis,
                offsetMillis = offsetMillis,
                createdAt = time
            )
        } catch (e: Exception) {
            logger.warn("TwitchChatWebSocketHandler: Failed to parse Twitch chat message (recordId: {}): {}", recordId, line, e)
            return null
        }
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        session.sendMessage(TextMessage(message))
    }

    private fun getTags(line: String): Map<String, String> {
        if (!line.startsWith("@")) return emptyMap()

        val tagsEnd = line.indexOf(" ")
        if (tagsEnd <= 0) return emptyMap()

        return line.substring(1, tagsEnd)
            .split(";")
            .mapNotNull { tag ->
                val parts = tag.split("=", limit = 2)
                if (parts.size != 2) return@mapNotNull null

                parts[0] to unescapeTagValue(parts[1])
            }
            .toMap()
    }

    /**
     * IRC 태그로 작성자 역할을 구한다.
     *
     * 방송인이 mod 태그도 받는지는 문서에 없어서 방송인 배지를 먼저 본다.
     * https://dev.twitch.tv/docs/chat/irc/#privmsg-tags
     */
    private fun getUserRole(tags: Map<String, String>): PlatformChatUserRole? {
        val badges = tags["badges"].orEmpty()
            .split(",")
            .map { it.substringBefore("/") }

        return when {
            "broadcaster" in badges -> PlatformChatUserRole.STREAMER
            tags["mod"] == "1" -> PlatformChatUserRole.MANAGER
            else -> null
        }
    }

    private fun unescapeTagValue(value: String): String {
        return value
            .replace("\\s", " ")
            .replace("\\:", ";")
            .replace("\\r", "\r")
            .replace("\\n", "\n")
            .replace("\\\\", "\\")
    }

    /**
     * IRC emotes 태그를 공통 이모지 DTO 목록으로 변환
     *
     * 같은 위치를 가리키는 이모트는 한 번만 담는다.
     */
    private fun getEmojis(
        tags: Map<String, String>,
        message: String
    ): List<PlatformChatEmojiDto> {
        val emotes = tags["emotes"]
        if (emotes.isNullOrBlank()) return emptyList()

        // 트위치가 주는 인덱스는 코드포인트 기준이라, 서로게이트 이모지가 섞여도 깨지지 않게 코드포인트로 다룬다
        val codePointLength = message.codePointCount(0, message.length)

        // emotes 태그 형식: emoteId:start-end,start-end/emoteId:start-end
        val emojis = mutableListOf<PlatformChatEmojiDto>()
        val addedKeys = mutableSetOf<String>()

        for (emote in emotes.split("/")) {
            val parts = emote.split(":", limit = 2)
            if (parts.size != 2) continue

            val emoteId = parts[0]
            for (range in parts[1].split(",")) {
                val indexes = range.split("-", limit = 2)
                if (indexes.size != 2) continue

                val start = indexes[0].toIntOrNull() ?: continue
                val end = indexes[1].toIntOrNull() ?: continue
                if (start < 0 || end < start || end >= codePointLength) continue

                val startIndex = message.offsetByCodePoints(0, start)
                val endIndex = message.offsetByCodePoints(0, end + 1)
                val placeholder = message.substring(startIndex, endIndex)
                val key = "$emoteId:$placeholder"
                if (!addedKeys.add(key)) continue

                emojis.add(
                    PlatformChatEmojiDto(
                        placeholder = placeholder,
                        imageUrl = getEmoteImageUrl(emoteId)
                    )
                )
            }
        }

        return emojis
    }

    private fun getEmoteImageUrl(emoteId: String): String {
        return "https://static-cdn.jtvnw.net/emoticons/v2/$emoteId/default/dark/2.0"
    }

}
