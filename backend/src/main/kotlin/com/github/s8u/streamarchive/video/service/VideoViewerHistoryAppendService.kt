package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoMetadataViewerHistory
import com.github.s8u.streamarchive.video.repository.VideoMetadataViewerHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 동영상 시청자 수 이력 적재 서비스
 */
@Service
class VideoViewerHistoryAppendService(
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveViewerCount(videoId: Long, viewerCount: Int, offsetMillis: Long) {
        viewerHistoryRepository.save(VideoMetadataViewerHistory(
            videoId = videoId,
            viewerCount = viewerCount,
            offsetMillis = offsetMillis
        ))
        logger.debug("VideoViewerHistoryAppendService: Saved viewer count: videoId={}, count={}, offset={}", videoId, viewerCount, offsetMillis)
    }

}
