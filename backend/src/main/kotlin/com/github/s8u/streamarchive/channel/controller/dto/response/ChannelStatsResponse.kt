package com.github.s8u.streamarchive.channel.controller.dto.response

import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelStatsResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 통계 응답")
data class ChannelStatsResponse(
    @field:Schema(description = "동영상 수", example = "42")
    val videoCount: Long,

    @field:Schema(description = "전체 파일 크기 (바이트)", example = "1073741824")
    val totalFileSize: Long
) {

    companion object {
        fun from(result: ChannelStatsResult): ChannelStatsResponse {
            return ChannelStatsResponse(
                videoCount = result.videoCount,
                totalFileSize = result.totalFileSize
            )
        }
    }
}
