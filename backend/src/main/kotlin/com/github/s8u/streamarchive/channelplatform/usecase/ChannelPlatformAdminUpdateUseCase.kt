package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.event.ChannelPlatformUpdatedEvent
import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminUpdateCommand
import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformAdminUpdateResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 수정 (관리자)
 */
@Service
class ChannelPlatformAdminUpdateUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlService: UrlService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun update(id: Long, command: ChannelPlatformAdminUpdateCommand): ChannelPlatformAdminUpdateResult {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        channelPlatform.update(
            platformChannelId = command.platformChannelId,
            isSyncProfile = command.isSyncProfile
        )

        // 프로필 동기화는 커밋 후 비동기로 처리한다 (외부 API 호출, 실패해도 수정은 성립)
        eventPublisher.publishEvent(ChannelPlatformUpdatedEvent(channelPlatform.channel?.id!!, channelPlatform.platformType))

        val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
        val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
        val channelProfileUrl = urlService.channelProfileUrl(channelPlatform.channel?.uuid!!)

        return ChannelPlatformAdminUpdateResult.from(channelPlatform, platformUrl, channelProfileUrl)
    }

}
