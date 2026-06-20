package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAdminSearchCommand
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAdminSearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 목록 조회 (관리자)
 */
@Service
class VideoAdminSearchUseCase(
    private val videoRepository: VideoRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: VideoAdminSearchCommand, pageable: Pageable): Page<VideoAdminSearchResult> {
        return videoRepository.searchForAdmin(command, pageable)
            .map { video ->
                VideoAdminSearchResult.from(
                    video = video,
                    channelProfileUrl = urlService.channelProfileUrl(video.channel?.uuid!!),
                    thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
                    playlistUrl = urlService.videoPlaylistUrl(video.uuid)
                )
            }
    }

}
