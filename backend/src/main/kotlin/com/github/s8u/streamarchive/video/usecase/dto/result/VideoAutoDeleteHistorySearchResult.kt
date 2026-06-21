package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 이력 조회 결과
 *
 * 파일이 사라져도 알 수 있게 삭제 시점 정보를 담는다.
 */
data class VideoAutoDeleteHistorySearchResult(
    val id: Long,
    val videoId: Long,
    val channelId: Long,
    val channelName: String,
    val title: String,
    val fileSize: Long,
    val videoCreatedAt: LocalDateTime,
    val deletedAt: LocalDateTime
) {

    companion object {
        fun from(
            history: VideoAutoDeleteHistory,
            channelName: String
        ): VideoAutoDeleteHistorySearchResult {
            return VideoAutoDeleteHistorySearchResult(
                id = history.id!!,
                videoId = history.videoId,
                channelId = history.channelId,
                channelName = channelName,
                title = history.title,
                fileSize = history.fileSize,
                videoCreatedAt = history.videoCreatedAt,
                deletedAt = history.createdAt
            )
        }
    }

}
