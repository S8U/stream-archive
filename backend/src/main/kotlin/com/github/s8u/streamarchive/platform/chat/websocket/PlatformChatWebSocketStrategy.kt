package com.github.s8u.streamarchive.platform.chat.websocket

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategy
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import jakarta.websocket.ContainerProvider
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.time.LocalDateTime

/**
 * WebSocket으로 채팅을 수집하는 전략의 공통 베이스
 *
 * WebSocket 연결과 세션 종료를 처리한다.
 * 하위 전략은 플랫폼별 연결 정보를 생성한다.
 */
abstract class PlatformChatWebSocketStrategy : PlatformChatStrategy {

    private val logger = LoggerFactory.getLogger(javaClass)

    protected abstract fun createConnection(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketConnection?

    override fun startCollecting(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatCollectionSession? {
        val connection = createConnection(
            recordId = recordId,
            videoId = videoId,
            platformChannelId = platformChannelId,
            recordStartedAt = recordStartedAt,
            onChat = onChat,
            onClosed = onClosed
        ) ?: return null

        logger.info(
            "PlatformChatWebSocketStrategy: Connecting chat WebSocket: recordId={}, platformType={}, url={}",
            recordId,
            platformType,
            connection.url
        )

        val webSocketContainer = ContainerProvider.getWebSocketContainer().apply {
            if (connection.binary) {
                defaultMaxBinaryMessageBufferSize = MAX_MESSAGE_BUFFER_SIZE
            } else {
                defaultMaxTextMessageBufferSize = MAX_MESSAGE_BUFFER_SIZE
            }
        }
        val session = StandardWebSocketClient(webSocketContainer)
            .execute(connection.handler, WebSocketHttpHeaders(), URI(connection.url))
            .get()

        return WebSocketChatCollectionSession(session, connection.handler)
    }

    companion object {
        private const val MAX_MESSAGE_BUFFER_SIZE = 1024 * 1024
    }

}
