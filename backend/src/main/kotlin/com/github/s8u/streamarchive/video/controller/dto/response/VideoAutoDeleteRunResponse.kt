package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeleteRunResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 자동 삭제 즉시 실행 응답 (관리자)")
data class VideoAutoDeleteRunResponse(
    @field:Schema(description = "삭제한 동영상 수", example = "42")
    val deletedCount: Int
) {

    companion object {
        fun from(result: VideoAutoDeleteRunResult): VideoAutoDeleteRunResponse {
            return VideoAutoDeleteRunResponse(
                deletedCount = result.deletedCount
            )
        }
    }

}
