package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatEmojiSearchResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 채팅 이모지 응답")
data class VideoChatEmojiSearchResponse(
    @field:Schema(description = "메시지 placeholder", example = "{:d_55:}")
    val placeholder: String,

    @field:Schema(description = "이모지 파일명", example = "d_55-a1b2c3d4e5f6.gif")
    val filename: String,

    @field:Schema(description = "이모지 이미지 URL")
    val imageUrl: String
) {

    companion object {
        fun from(result: VideoChatEmojiSearchResult): VideoChatEmojiSearchResponse {
            return VideoChatEmojiSearchResponse(
                placeholder = result.placeholder,
                filename = result.filename,
                imageUrl = result.imageUrl
            )
        }
    }

}
