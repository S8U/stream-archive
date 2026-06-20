package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 동영상 제목 갱신 서비스
 *
 * 동영상이 없으면 아무것도 하지 않는다.
 */
@Service
class VideoTitleUpdateService(
    private val videoRepository: VideoRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 동영상의 제목을 갱신한다.
     */
    fun update(videoId: Long, title: String) {
        val video = videoRepository.findById(videoId).orElse(null) ?: return
        video.changeTitle(title)
        videoRepository.save(video)

        logger.debug("VideoTitleUpdateService: Updated active recording video title: videoId={}, title={}", videoId, title)
    }

}
