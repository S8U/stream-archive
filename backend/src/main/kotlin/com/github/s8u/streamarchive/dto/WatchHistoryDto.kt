package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.UserVideoWatchHistory
import com.github.s8u.streamarchive.entity.Video
import java.time.LocalDateTime

// 시청 위치 저장 요청
data class SaveWatchHistoryRequest(
    val position: Int  // 재생 위치 (초)
)

// 개별 영상 시청 기록 응답
data class WatchHistoryResponse(
    val lastPosition: Int,
    val watchedAt: LocalDateTime
) {
    companion object {
        fun from(history: UserVideoWatchHistory): WatchHistoryResponse {
            return WatchHistoryResponse(
                lastPosition = history.lastPosition,
                watchedAt = history.watchedAt
            )
        }
    }
}

// 시청 기록 목록 아이템
data class WatchHistoryListResponse(
    val channel: ChannelInfo,
    val video: VideoInfo,
    val lastPosition: Int,
    val progress: Int,  // 진행률 (%)
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
        fun from(
            history: UserVideoWatchHistory,
            video: Video,
            channelProfileUrl: String,
            videoThumbnailUrl: String
        ): WatchHistoryListResponse {
            val channel = video.channel!!
            val progress = if (video.duration > 0) {
                (history.lastPosition.toDouble() / video.duration * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            return WatchHistoryListResponse(
                channel = ChannelInfo(
                    uuid = channel.uuid,
                    name = channel.name,
                    profileUrl = channelProfileUrl
                ),
                video = VideoInfo(
                    uuid = video.uuid,
                    title = video.title,
                    thumbnailUrl = videoThumbnailUrl,
                    duration = video.duration
                ),
                lastPosition = history.lastPosition,
                progress = progress,
                watchedAt = history.watchedAt
            )
        }
    }
}
