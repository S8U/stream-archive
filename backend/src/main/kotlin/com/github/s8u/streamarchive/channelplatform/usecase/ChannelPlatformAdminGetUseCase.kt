package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformAdminGetResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 상세 조회 (관리자)
 */
@Service
class ChannelPlatformAdminGetUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun get(id: Long): ChannelPlatformAdminGetResult {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
        val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
        val channelProfileUrl = urlService.channelProfileUrl(channelPlatform.channel?.uuid!!)

        return ChannelPlatformAdminGetResult.from(channelPlatform, platformUrl, channelProfileUrl)
    }

}
