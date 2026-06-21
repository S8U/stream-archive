package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePolicyGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 자동 삭제 정책 조회 응답 (관리자)")
data class VideoAutoDeletePolicyGetResponse(
    @field:Schema(description = "채널 ID (null이면 전체 기본 정책)", example = "1")
    val channelId: Long?,

    @field:Schema(description = "정책 설정 여부 (false면 아직 설정한 적 없음)", example = "true")
    val isConfigured: Boolean,

    @field:Schema(description = "자동 삭제 활성화 여부", example = "true")
    val isEnabled: Boolean,

    @field:Schema(description = "생성 후 며칠 지난 동영상을 삭제할지", example = "30")
    val deleteAfterDays: Int?
) {

    companion object {
        fun from(result: VideoAutoDeletePolicyGetResult): VideoAutoDeletePolicyGetResponse {
            return VideoAutoDeletePolicyGetResponse(
                channelId = result.channelId,
                isConfigured = result.isConfigured,
                isEnabled = result.isEnabled,
                deleteAfterDays = result.deleteAfterDays
            )
        }
    }

}
