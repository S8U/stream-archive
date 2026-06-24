package com.github.s8u.streamarchive.platform.chat.dto

import java.time.LocalDateTime

data class PlatformChatMessageDto(
    val recordId: Long,
    val videoId: Long,
    val username: String,
    val message: String,
    val emojis: List<PlatformChatEmojiDto> = emptyList(),
    val offsetMillis: Long,
    val createdAt: LocalDateTime
)
