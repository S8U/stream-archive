package com.github.s8u.streamarchive.platform.chat.websocket

/**
 * WebSocket 채팅 연결 정보
 *
 * [binary]가 true면 바이너리 버퍼를, false면 텍스트 버퍼를 키운다.
 */
data class PlatformChatWebSocketConnection(
    val url: String,
    val handler: PlatformChatWebSocketHandler,
    val binary: Boolean = false
)
