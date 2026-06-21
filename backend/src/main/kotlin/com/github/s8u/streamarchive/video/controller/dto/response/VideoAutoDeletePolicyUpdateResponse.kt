package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePolicyUpdateResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 자동 삭제 정책 설정 응답 (관리자)")
data class VideoAutoDeletePolicyUpdateResponse(
    @field:Schema(description = "채널 ID (null이면 전체 기본 정책)", example = "1")
    val channelId: Long?,

    @field:Schema(description = "자동 삭제 활성화 여부", example = "true")
    val isEnabled: Boolean,

    @field:Schema(description = "생성 후 며칠 지난 동영상을 삭제할지", example = "30")
    val deleteAfterDays: Int
) {

    companion object {
        fun from(result: VideoAutoDeletePolicyUpdateResult): VideoAutoDeletePolicyUpdateResponse {
            return VideoAutoDeletePolicyUpdateResponse(
                channelId = result.channelId,
                isEnabled = result.isEnabled,
                deleteAfterDays = result.deleteAfterDays
            )
        }
    }

}
