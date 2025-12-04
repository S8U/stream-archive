package com.github.s8u.streamarchive.chat

import java.time.LocalDateTime

data class ChatMessageDto(
    val recordId: Long,
    val videoId: Long,
    val username: String,
    val message: String,
    val offsetMillis: Long,
    val createdAt: LocalDateTime
)