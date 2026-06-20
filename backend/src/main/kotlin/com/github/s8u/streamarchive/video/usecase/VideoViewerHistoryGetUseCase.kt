package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoMetadataViewerHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoViewerHistoryGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청자 수 이력 조회 (공개)
 */
@Service
class VideoViewerHistoryGetUseCase(
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository,
    private val videoRepository: VideoRepository,
    private val videoAccessAssertService: VideoAccessAssertService
) {

    @Transactional(readOnly = true)
    fun getByVideoUuid(uuid: String): List<VideoViewerHistoryGetResult> {
        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        return viewerHistoryRepository.findByVideoIdOrderByOffsetMillisAsc(video.id!!)
            .map { VideoViewerHistoryGetResult.from(it) }
    }

}
