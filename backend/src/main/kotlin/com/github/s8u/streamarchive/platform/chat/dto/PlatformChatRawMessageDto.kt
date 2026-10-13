package com.github.s8u.streamarchive.platform.chat.dto

import com.github.s8u.streamarchive.platform.enums.PlatformChatRawFormat
import java.time.LocalDateTime

/**
 * 플랫폼 채팅 원본 메시지
 *
 * 플랫폼에서 받은 응답을 가공하지 않고 담는다.
 * WebSocket은 프레임 하나, 폴링은 응답 본문 하나가 메시지 하나다.
 */
data class PlatformChatRawMessageDto(
    val format: PlatformChatRawFormat,
    val payload: String,
    val offsetMillis: Long,
    val receivedAt: LocalDateTime
)
