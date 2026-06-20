package com.github.s8u.streamarchive.dashboard.controller.dto.response

import com.github.s8u.streamarchive.dashboard.usecase.dto.result.DashboardStatGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "대시보드 통계 응답 (관리자)")
data class DashboardStatResponse(
    @field:Schema(description = "전체 채널 수", example = "10")
    val totalChannels: Long,

    @field:Schema(description = "전체 동영상 수", example = "100")
    val totalVideos: Long,

    @field:Schema(description = "전체 재생 시간 (초)", example = "360000")
    val totalDuration: Long,

    @field:Schema(description = "전체 스토리지 사용량 (바이트)", example = "1073741824")
    val totalStorage: Long
) {

    companion object {
        fun from(result: DashboardStatGetResult): DashboardStatResponse {
            return DashboardStatResponse(
                totalChannels = result.totalChannels,
                totalVideos = result.totalVideos,
                totalDuration = result.totalDuration,
                totalStorage = result.totalStorage
            )
        }
    }
}
