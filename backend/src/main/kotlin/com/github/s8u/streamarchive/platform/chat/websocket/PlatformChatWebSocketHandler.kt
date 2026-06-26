package com.github.s8u.streamarchive.platform.chat.websocket

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import org.springframework.web.socket.WebSocketHandler
import java.time.LocalDateTime

abstract class PlatformChatWebSocketHandler(
    val recordId: Long,
    val videoId: Long,
    val platformChannelId: String,
    val recordStartedAt: LocalDateTime,
    val onChat: (chatMessageDto: PlatformChatMessageDto) -> Unit,
    val onConnectionClosed: () -> Unit
) : WebSocketHandler {

    /**
     * 세션 종료 시 핸들러가 보유한 리소스를 정리한다.
     *
     * keep-alive 스레드 등 연결과 함께 떠 있는 리소스를 가진 핸들러가 오버라이드한다.
     */
    open fun stop() {
    }

}
