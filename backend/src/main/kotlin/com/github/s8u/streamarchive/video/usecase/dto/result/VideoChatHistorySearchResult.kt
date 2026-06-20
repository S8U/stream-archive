package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoChatHistory

/**
 * 동영상 채팅 이력 조회 결과
 */
data class VideoChatHistorySearchResult(
    val username: String,
    val message: String,
    val offsetMillis: Long
) {

    companion object {
        fun from(chatHistory: VideoChatHistory): VideoChatHistorySearchResult {
            return VideoChatHistorySearchResult(
                username = chatHistory.username,
                message = chatHistory.message,
                offsetMillis = chatHistory.offsetMillis
            )
        }
    }
}
