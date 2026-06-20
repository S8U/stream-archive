package com.github.s8u.streamarchive.watchhistory.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.watchhistory.entity.UserVideoWatchHistory
import java.time.LocalDateTime

/**
 * 시청 기록 목록 아이템 조회 결과
 */
data class WatchHistorySearchResult(
    val channel: ChannelInfo,
    val video: VideoInfo,
    val lastPosition: Int,
    val progress: Int,
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
        ): WatchHistorySearchResult {
            val channel = video.channel!!
            val progress = if (video.duration > 0) {
                (history.lastPosition.toDouble() / video.duration * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            return WatchHistorySearchResult(
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
