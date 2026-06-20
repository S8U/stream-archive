package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoViewerHistoryGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 시청자 수 이력 응답")
data class VideoViewerHistoryGetResponse(
    @field:Schema(description = "시청자 수", example = "1234")
    val viewerCount: Int,

    @field:Schema(description = "동영상 시작 기준 오프셋 (밀리초)", example = "12000")
    val offsetMillis: Long
) {

    companion object {
        fun from(result: VideoViewerHistoryGetResult): VideoViewerHistoryGetResponse {
            return VideoViewerHistoryGetResponse(
                viewerCount = result.viewerCount,
                offsetMillis = result.offsetMillis
            )
        }
    }
}
