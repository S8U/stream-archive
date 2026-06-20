package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.service.ChannelAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoMetadataViewerHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoSearchCommand
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoSearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 목록 조회 (공개)
 */
@Service
class VideoSearchUseCase(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository,
    private val channelAccessAssertService: ChannelAccessAssertService,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: VideoSearchCommand, pageable: Pageable): Page<VideoSearchResult> {
        command.channelUuid?.let { channelUuid ->
            val channel = channelRepository.findByUuid(channelUuid) ?: throw BusinessException(
                "채널을 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND
            )
            channelAccessAssertService.assertAccessible(channel.contentPrivacy)
        }

        val videos = videoRepository.searchForPublic(command, pageable)

        // 페이지 내 동영상들의 피크 시청자 수를 한 번에 조회 (N+1 방지)
        val peakViewerCounts = viewerHistoryRepository
            .findPeakViewerCountsByVideoIds(videos.content.mapNotNull { it.id })
            .associate { it.videoId to it.peakViewerCount }

        return videos.map { video ->
            if (video.channel == null) {
                throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

            VideoSearchResult.from(
                video = video,
                channelProfileUrl = urlService.channelProfileUrl(video.channel!!.uuid),
                thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
                playlistUrl = urlService.videoPlaylistUrl(video.uuid),
                peakViewerCount = peakViewerCounts[video.id]
            )
        }
    }

}
