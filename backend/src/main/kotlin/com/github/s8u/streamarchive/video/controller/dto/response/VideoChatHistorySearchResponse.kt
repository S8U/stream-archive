package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatHistorySearchResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 채팅 이력 응답")
data class VideoChatHistorySearchResponse(
    @field:Schema(description = "사용자명")
    val username: String,

    @field:Schema(description = "메시지")
    val message: String,

    @field:Schema(description = "동영상 시작 기준 오프셋 (밀리초)", example = "12000")
    val offsetMillis: Long
) {

    companion object {
        fun from(result: VideoChatHistorySearchResult): VideoChatHistorySearchResponse {
            return VideoChatHistorySearchResponse(
                username = result.username,
                message = result.message,
                offsetMillis = result.offsetMillis
            )
        }
    }
}
