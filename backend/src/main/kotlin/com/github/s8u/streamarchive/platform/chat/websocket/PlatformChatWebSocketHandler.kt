package com.github.s8u.streamarchive.platform.chat.websocket

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatRawFormat
import org.slf4j.LoggerFactory
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.LocalDateTime
import java.util.Base64

/**
 * 플랫폼 채팅 WebSocket 핸들러의 공통 베이스
 *
 * 받은 프레임을 원본 그대로 넘긴 뒤 플랫폼별 처리를 맡긴다.
 */
abstract class PlatformChatWebSocketHandler(
    val recordId: Long,
    val videoId: Long,
    val platformChannelId: String,
    val recordStartedAt: LocalDateTime,
    val onChat: (chatMessageDto: PlatformChatMessageDto) -> Unit,
    val onRaw: (rawMessageDto: PlatformChatRawMessageDto) -> Unit,
    val onConnectionClosed: () -> Unit
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    final override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        // 원본 보관이 실패해도 채팅 수집은 계속한다
        try {
            toRawMessage(message)?.let(onRaw)
        } catch (e: Exception) {
            logger.warn("PlatformChatWebSocketHandler: Failed to pass chat raw message: recordId={}", recordId, e)
        }

        handlePlatformMessage(session, message)
    }

    /**
     * 플랫폼별로 받은 프레임을 처리한다.
     */
    protected abstract fun handlePlatformMessage(session: WebSocketSession, message: WebSocketMessage<*>)

    /**
     * 세션 종료 시 핸들러가 보유한 리소스를 정리한다.
     *
     * keep-alive 스레드 등 연결과 함께 떠 있는 리소스를 가진 핸들러가 오버라이드한다.
     */
    open fun stop() {
    }

    private fun toRawMessage(message: WebSocketMessage<*>): PlatformChatRawMessageDto? {
        val receivedAt = LocalDateTime.now()
        val offsetMillis = Duration.between(recordStartedAt, receivedAt).toMillis()

        return when (message) {
            is TextMessage -> PlatformChatRawMessageDto(
                format = PlatformChatRawFormat.TEXT,
                payload = message.payload,
                offsetMillis = offsetMillis,
                receivedAt = receivedAt
            )

            is BinaryMessage -> {
                // 하위 핸들러가 같은 버퍼를 다시 읽으므로 위치를 건드리지 않는 복제본으로 읽는다
                val buffer = message.payload.duplicate()
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                PlatformChatRawMessageDto(
                    format = PlatformChatRawFormat.BINARY,
                    payload = Base64.getEncoder().encodeToString(bytes),
                    offsetMillis = offsetMillis,
                    receivedAt = receivedAt
                )
            }

            // ping/pong 제어 프레임은 채팅 내용이 없어 남기지 않는다
            else -> null
        }
    }

}
