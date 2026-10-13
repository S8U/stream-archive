package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import com.github.s8u.streamarchive.video.entity.VideoChatHistory

/**
 * 동영상 채팅 이력 조회 결과
 */
data class VideoChatHistorySearchResult(
    val messageType: PlatformChatMessageType,
    val username: String,
    val userRole: PlatformChatUserRole?,
    val message: String,
    val donationAmount: Int?,
    val emojis: List<VideoChatEmojiSearchResult>,
    val offsetMillis: Long
) {

    companion object {
        fun from(
            chatHistory: VideoChatHistory,
            emojis: List<VideoChatEmojiSearchResult>
        ): VideoChatHistorySearchResult {
            return VideoChatHistorySearchResult(
                messageType = chatHistory.messageType,
                username = chatHistory.username,
                userRole = chatHistory.userRole,
                message = chatHistory.message,
                donationAmount = chatHistory.donationAmount,
                emojis = emojis,
                offsetMillis = chatHistory.offsetMillis
            )
        }
    }

}
