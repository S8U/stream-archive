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
}
