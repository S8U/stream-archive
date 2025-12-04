package com.github.s8u.streamarchive.chat

import org.springframework.web.socket.WebSocketHandler
import java.time.LocalDateTime

abstract class ChatWebSocketHandler(
    val recordId: Long,
    val videoId: Long,
    val platformChannelId: String,
    val recordStartedAt: LocalDateTime,
    val onChat: (chatMessageDto: ChatMessageDto) -> Unit
) : WebSocketHandler {
}