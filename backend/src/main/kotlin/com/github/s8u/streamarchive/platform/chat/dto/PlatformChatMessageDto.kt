package com.github.s8u.streamarchive.platform.chat.dto

import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import java.time.LocalDateTime

data class PlatformChatMessageDto(
    val recordId: Long,
    val videoId: Long,
    val messageType: PlatformChatMessageType = PlatformChatMessageType.CHAT,
    val username: String,
    val userRole: PlatformChatUserRole? = null,
    val message: String,
    val donationAmount: Int? = null,
    val emojis: List<PlatformChatEmojiDto> = emptyList(),
    val offsetMillis: Long,
    val createdAt: LocalDateTime
)
