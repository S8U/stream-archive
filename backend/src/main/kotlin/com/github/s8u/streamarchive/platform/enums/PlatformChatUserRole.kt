package com.github.s8u.streamarchive.platform.enums

/**
 * 플랫폼 채팅 작성자 역할
 *
 * 일반 시청자는 역할이 없다.
 */
enum class PlatformChatUserRole {
    STREAMER,  // 방송 채널 주인
    MANAGER    // 채팅이나 채널을 관리하는 운영자
}
