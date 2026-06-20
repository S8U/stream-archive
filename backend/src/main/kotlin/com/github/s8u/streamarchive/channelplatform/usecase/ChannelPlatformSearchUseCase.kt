package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.service.ChannelAccessAssertService
import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformSearchResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널의 플랫폼 목록 조회 (공개)
 */
@Service
class ChannelPlatformSearchUseCase(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelAccessAssertService: ChannelAccessAssertService,
    private val platformStrategyFactory: PlatformStrategyFactory
) {

    @Transactional(readOnly = true)
    fun searchByChannelUuid(channelUuid: String): List<ChannelPlatformSearchResult> {
        val channel = channelRepository.findByUuid(channelUuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        channelAccessAssertService.assertAccessible(channel.contentPrivacy)

        return channelPlatformRepository.findByChannelId(channel.id!!)
            .map { channelPlatform ->
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val streamUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)

                ChannelPlatformSearchResult.from(channelPlatform, streamUrl)
            }
    }

}
