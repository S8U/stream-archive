package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePreviewSummaryGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 자동 삭제 미리보기 요약 응답 (관리자)")
data class VideoAutoDeletePreviewSummaryGetResponse(
    @field:Schema(description = "채널 ID (null이면 전체)", example = "1")
    val channelId: Long?,

    @field:Schema(description = "다음 자동 삭제 대상 동영상 수", example = "42")
    val targetCount: Long,

    @field:Schema(description = "다음 자동 삭제 대상 총 파일 크기 (바이트)", example = "335007449088")
    val totalFileSize: Long
) {

    companion object {
        fun from(result: VideoAutoDeletePreviewSummaryGetResult): VideoAutoDeletePreviewSummaryGetResponse {
            return VideoAutoDeletePreviewSummaryGetResponse(
                channelId = result.channelId,
                targetCount = result.targetCount,
                totalFileSize = result.totalFileSize
            )
        }
    }

}
