package com.github.s8u.streamarchive.platform.chat.websocket

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import org.springframework.web.socket.WebSocketSession

/**
 * WebSocket 채팅 수집 세션
 */
class WebSocketChatCollectionSession(
    private val session: WebSocketSession
) : PlatformChatCollectionSession {

    override fun stop() {
        if (session.isOpen) {
            session.close()
        }
    }

}
