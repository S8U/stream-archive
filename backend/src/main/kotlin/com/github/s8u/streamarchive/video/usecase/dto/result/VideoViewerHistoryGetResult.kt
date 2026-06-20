package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoMetadataViewerHistory

/**
 * 동영상 시청자 수 이력 조회 결과
 */
data class VideoViewerHistoryGetResult(
    val viewerCount: Int,
    val offsetMillis: Long
) {

    companion object {
        fun from(viewerHistory: VideoMetadataViewerHistory): VideoViewerHistoryGetResult {
            return VideoViewerHistoryGetResult(
                viewerCount = viewerHistory.viewerCount,
                offsetMillis = viewerHistory.offsetMillis
            )
        }
    }
}
