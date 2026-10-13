package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

/**
 * 치지직 채팅 메시지 종류 코드
 *
 * 채팅 메시지의 `msgTypeCode` 값이다.
 */
enum class ChzzkChatMessageTypeCode(
    val value: Int
) {
    TEXT(1),     // 일반 채팅
    DONATION(10) // 후원
}
