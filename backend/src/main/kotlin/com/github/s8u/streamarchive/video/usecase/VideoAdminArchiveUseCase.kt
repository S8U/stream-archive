package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.global.util.RequestUtils
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoArchiveService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAdminArchiveResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 소장 여부 설정 (관리자)
 */
@Service
class VideoAdminArchiveUseCase(
    private val videoRepository: VideoRepository,
    private val videoArchiveService: VideoArchiveService,
    private val currentUserService: CurrentUserService,
    private val urlService: UrlService
) {

    @Transactional
    fun setArchived(id: Long, isArchived: Boolean): VideoAdminArchiveResult {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val userId = currentUserService.getCurrentUserId()
        val clientIp = RequestUtils.getClientIp()

        videoArchiveService.setArchived(video, isArchived, userId, clientIp)

        return VideoAdminArchiveResult.from(
            video = video,
            channelProfileUrl = urlService.channelProfileUrl(video.channel?.uuid!!),
            thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
            playlistUrl = urlService.videoPlaylistUrl(video.uuid)
        )
    }

}
