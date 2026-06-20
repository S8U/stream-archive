package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminSearchResult
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 목록 조회 (관리자)
 */
@Service
class ChannelAdminSearchUseCase(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: ChannelAdminSearchCommand, pageable: Pageable): Page<ChannelAdminSearchResult> {
        val channels = channelRepository.searchForAdmin(command, pageable)
        val totalVideoFileSizes = videoRepository.sumFileSizeByChannelIds(channels.content.mapNotNull { it.id })

        return channels.map { channel ->
            ChannelAdminSearchResult.from(
                channel = channel,
                profileUrl = urlService.channelProfileUrl(channel.uuid),
                totalVideoFileSize = totalVideoFileSizes[channel.id] ?: 0L
            )
        }
    }

}
