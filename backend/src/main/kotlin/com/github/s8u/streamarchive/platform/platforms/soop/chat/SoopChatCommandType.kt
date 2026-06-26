package com.github.s8u.streamarchive.platform.platforms.soop.chat

/**
 * SOOP 채팅 명령 타입
 */
enum class SoopChatCommandType(
    val value: Int
) {
    KEEP_ALIVE(0),
    LOGIN(1),
    JOIN_CHANNEL(2),
    CHAT_MESSAGE(5),
    OGQ_EMOTICON(109)
}
