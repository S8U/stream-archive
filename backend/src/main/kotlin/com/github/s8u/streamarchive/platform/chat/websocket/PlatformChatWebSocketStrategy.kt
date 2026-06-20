package com.github.s8u.streamarchive.platform.chat.websocket

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategy
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.time.LocalDateTime

/**
 * WebSocket으로 채팅을 수집하는 전략의 공통 베이스
 *
 * WebSocket 연결·세션 종료를 여기서 처리하고, 하위 전략은 접속 URL과
 * 플랫폼별 핸들러 생성만 정의한다.
 */
abstract class PlatformChatWebSocketStrategy : PlatformChatStrategy {

    protected abstract val chatWebSocketUrl: String

    protected abstract fun createHandler(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketHandler

    override fun startCollecting(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatCollectionSession {
        val handler = createHandler(recordId, videoId, platformChannelId, recordStartedAt, onChat, onClosed)
        val session = StandardWebSocketClient().execute(handler, null, URI(chatWebSocketUrl)).get()

        return WebSocketChatCollectionSession(session)
    }

    private class WebSocketChatCollectionSession(
        private val session: WebSocketSession
    ) : PlatformChatCollectionSession {

        override fun stop() {
            if (session.isOpen) {
                session.close()
            }
        }

    }

}
