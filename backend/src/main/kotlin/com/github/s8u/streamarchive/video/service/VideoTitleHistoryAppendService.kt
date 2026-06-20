package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoMetadataTitleHistory
import com.github.s8u.streamarchive.video.repository.VideoMetadataTitleHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 동영상 제목 변경 이력 적재 서비스
 */
@Service
class VideoTitleHistoryAppendService(
    private val titleHistoryRepository: VideoMetadataTitleHistoryRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveTitle(videoId: Long, title: String, offsetMillis: Long) {
        titleHistoryRepository.save(VideoMetadataTitleHistory(
            videoId = videoId,
            title = title,
            offsetMillis = offsetMillis
        ))
        logger.info("VideoTitleHistoryAppendService: Saved title: videoId={}, title={}, offset={}", videoId, title, offsetMillis)
    }

}
