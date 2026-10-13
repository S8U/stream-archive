package com.github.s8u.streamarchive.platform.enums

/**
 * 플랫폼 채팅 원본 형식
 */
enum class PlatformChatRawFormat {
    TEXT,   // 받은 문자열 그대로
    BINARY  // 받은 바이트를 Base64로 인코딩
}
