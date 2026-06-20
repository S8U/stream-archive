package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAdminGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 단건 조회 (관리자)
 */
@Service
class VideoAdminGetUseCase(
    private val videoRepository: VideoRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun get(id: Long): VideoAdminGetResult {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return VideoAdminGetResult.from(
            video = video,
            channelProfileUrl = urlService.channelProfileUrl(video.channel?.uuid!!),
            thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
            playlistUrl = urlService.videoPlaylistUrl(video.uuid)
        )
    }

}
