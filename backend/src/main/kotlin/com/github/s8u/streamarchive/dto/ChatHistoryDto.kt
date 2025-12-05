package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.VideoDataChatHistory

data class ChatHistoryResponse(
    val username: String,
    val message: String,
    val offsetMillis: Long
) {
    companion object {
        fun from(chatHistory: VideoDataChatHistory): ChatHistoryResponse {
            return ChatHistoryResponse(
                username = chatHistory.username,
                message = chatHistory.message,
                offsetMillis = chatHistory.offsetMillis
            )
        }
    }
}
