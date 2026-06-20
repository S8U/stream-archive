package com.github.s8u.streamarchive.watchhistory.controller.dto.response

import com.github.s8u.streamarchive.watchhistory.usecase.dto.result.WatchHistorySearchResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "시청 기록 목록 응답")
data class WatchHistorySearchResponse(
    @field:Schema(description = "채널 정보")
    val channel: ChannelInfo,

    @field:Schema(description = "동영상 정보")
    val video: VideoInfo,

    @field:Schema(description = "마지막 재생 위치 (초)", example = "120")
    val lastPosition: Int,

    @field:Schema(description = "진행률 (%)", example = "50")
    val progress: Int,

    @field:Schema(description = "시청 일시")
    val watchedAt: LocalDateTime
) {

    data class ChannelInfo(
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    data class VideoInfo(
        val uuid: String,
        val title: String,
        val thumbnailUrl: String,
        val duration: Int
    )

    companion object {
        fun from(result: WatchHistorySearchResult): WatchHistorySearchResponse {
            return WatchHistorySearchResponse(
                channel = ChannelInfo(
                    uuid = result.channel.uuid,
                    name = result.channel.name,
                    profileUrl = result.channel.profileUrl
                ),
                video = VideoInfo(
                    uuid = result.video.uuid,
                    title = result.video.title,
                    thumbnailUrl = result.video.thumbnailUrl,
                    duration = result.video.duration
                ),
                lastPosition = result.lastPosition,
                progress = result.progress,
                watchedAt = result.watchedAt
            )
        }
    }
}
