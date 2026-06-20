package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoMetadataCategoryHistory
import com.github.s8u.streamarchive.video.repository.VideoMetadataCategoryHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 동영상 카테고리 변경 이력 적재 서비스
 */
@Service
class VideoCategoryHistoryAppendService(
    private val categoryHistoryRepository: VideoMetadataCategoryHistoryRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveCategory(videoId: Long, category: String?, offsetMillis: Long) {
        categoryHistoryRepository.save(VideoMetadataCategoryHistory(
            videoId = videoId,
            category = category,
            offsetMillis = offsetMillis
        ))
        logger.info("VideoCategoryHistoryAppendService: Saved category: videoId={}, category={}, offset={}", videoId, category, offsetMillis)
    }

}
