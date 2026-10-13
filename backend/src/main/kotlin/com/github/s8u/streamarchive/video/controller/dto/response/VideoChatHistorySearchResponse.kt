package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatHistorySearchResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 채팅 이력 응답")
data class VideoChatHistorySearchResponse(
    @field:Schema(description = "메시지 종류", example = "CHAT")
    val messageType: PlatformChatMessageType,

    @field:Schema(description = "사용자명")
    val username: String,

    @field:Schema(description = "작성자 역할 (없으면 일반 시청자)", example = "MANAGER")
    val userRole: PlatformChatUserRole?,

    @field:Schema(description = "메시지")
    val message: String,

    @field:Schema(description = "후원 금액 (후원이 아니면 null)", example = "1000")
    val donationAmount: Int?,

    @field:Schema(description = "이모지 목록")
    val emojis: List<VideoChatEmojiSearchResponse>,

    @field:Schema(description = "동영상 시작 기준 오프셋 (밀리초)", example = "12000")
    val offsetMillis: Long
) {

    companion object {
        fun from(result: VideoChatHistorySearchResult): VideoChatHistorySearchResponse {
            return VideoChatHistorySearchResponse(
                messageType = result.messageType,
                username = result.username,
                userRole = result.userRole,
                message = result.message,
                donationAmount = result.donationAmount,
                emojis = result.emojis.map { VideoChatEmojiSearchResponse.from(it) },
                offsetMillis = result.offsetMillis
            )
        }
    }

}
