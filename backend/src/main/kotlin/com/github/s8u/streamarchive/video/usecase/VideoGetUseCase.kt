package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryGetService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 단건 조회 (공개)
 */
@Service
class VideoGetUseCase(
    private val videoRepository: VideoRepository,
    private val viewerHistoryGetService: VideoViewerHistoryGetService,
    private val videoAccessAssertService: VideoAccessAssertService,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String): VideoGetResult {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val peakViewerHistory = viewerHistoryGetService.findPeak(video.id!!)

        return VideoGetResult.from(
            video = video,
            channelProfileUrl = urlService.channelProfileUrl(video.channel!!.uuid),
            thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
            playlistUrl = urlService.videoPlaylistUrl(video.uuid),
            peakViewerCount = peakViewerHistory?.viewerCount,
            peakViewerOffsetMillis = peakViewerHistory?.offsetMillis
        )
    }

}
