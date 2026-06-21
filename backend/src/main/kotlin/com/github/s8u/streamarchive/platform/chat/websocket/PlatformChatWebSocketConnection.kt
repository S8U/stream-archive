package com.github.s8u.streamarchive.platform.chat.websocket

/**
 * WebSocket 채팅 연결 정보
 */
data class PlatformChatWebSocketConnection(
    val url: String,
    val handler: PlatformChatWebSocketHandler
)
