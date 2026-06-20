package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelSearchResult
import com.github.s8u.streamarchive.global.service.UrlService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 목록 조회 (공개)
 */
@Service
class ChannelSearchUseCase(
    private val channelRepository: ChannelRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: ChannelSearchCommand, pageable: Pageable): Page<ChannelSearchResult> {
        return channelRepository.searchForPublic(command, pageable)
            .map { channel ->
                ChannelSearchResult.from(
                    channel = channel,
                    profileUrl = urlService.channelProfileUrl(channel.uuid)
                )
            }
    }

}
