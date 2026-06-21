package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChapterGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 챕터 응답")
data class VideoChapterGetResponse(
    @field:Schema(description = "동영상 시작 기준 오프셋 (밀리초)", example = "12000")
    val offsetMillis: Long,

    @field:Schema(description = "카테고리", example = "발로란트")
    val category: String?,

    @field:Schema(description = "챕터 시작 시점의 제목", example = "발로란트 랭크 게임")
    val title: String?
) {

    companion object {
        fun from(result: VideoChapterGetResult): VideoChapterGetResponse {
            return VideoChapterGetResponse(
                offsetMillis = result.offsetMillis,
                category = result.category,
                title = result.title
            )
        }
    }
}
