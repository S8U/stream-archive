package com.github.s8u.streamarchive.video.repository.dto

/**
 * 동영상 피크 시청자 수 투영
 *
 * 동영상별 최고 시청자 수를 담는다.
 */
data class VideoPeakViewerProjection(
    val videoId: Long,
    val peakViewerCount: Int
)
