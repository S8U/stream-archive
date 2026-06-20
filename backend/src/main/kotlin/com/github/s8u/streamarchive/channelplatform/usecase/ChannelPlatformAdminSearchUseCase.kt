package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminSearchCommand
import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformAdminSearchResult
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 목록 조회 (관리자)
 */
@Service
class ChannelPlatformAdminSearchUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: ChannelPlatformAdminSearchCommand, pageable: Pageable): Page<ChannelPlatformAdminSearchResult> {
        return channelPlatformRepository.searchForAdmin(command, pageable)
            .map { channelPlatform ->
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
                val channelProfileUrl = urlService.channelProfileUrl(channelPlatform.channel?.uuid!!)

                ChannelPlatformAdminSearchResult.from(channelPlatform, platformUrl, channelProfileUrl)
            }
    }

}
