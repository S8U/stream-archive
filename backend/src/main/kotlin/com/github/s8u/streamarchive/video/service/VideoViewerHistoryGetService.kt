package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoMetadataViewerHistory
import com.github.s8u.streamarchive.video.repository.VideoMetadataViewerHistoryRepository
import org.springframework.stereotype.Service

/**
 * 동영상 시청자 수 이력 조회 서비스
 */
@Service
class VideoViewerHistoryGetService(
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository
) {

    /**
     * 시청자 수가 가장 높았던 이력을 반환한다.
     */
    fun findPeak(videoId: Long): VideoMetadataViewerHistory? {
        return viewerHistoryRepository.findTopByVideoIdOrderByViewerCountDescOffsetMillisAsc(videoId)
    }

}
