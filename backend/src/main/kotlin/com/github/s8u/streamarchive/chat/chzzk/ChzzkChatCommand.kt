package com.github.s8u.streamarchive.chat.chzzk

enum class ChzzkChatCommand(
    val value: Int
) {
    PING(0),
    PONG(10000),
    REQUEST_CONNECT(100),
    RESPONSE_CONNECT(10100),
    SEND_CHAT(3101),
    REQUEST_RECENT_CHAT(5101),
    RESPONSE_RECENT_CHAT(15101),
    CHAT(93101),
    DONATION(93102)
}