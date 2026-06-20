package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.event.ChannelPlatformCreatedEvent
import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminCreateCommand
import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformAdminCreateResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 생성 (관리자)
 */
@Service
class ChannelPlatformAdminCreateUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelRepository: ChannelRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlService: UrlService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun create(command: ChannelPlatformAdminCreateCommand): ChannelPlatformAdminCreateResult {
        val channel = channelRepository.findById(command.channelId).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val channelPlatform = ChannelPlatform(
            channel = channel,
            platformType = command.platformType,
            platformChannelId = command.platformChannelId,
            isSyncProfile = command.isSyncProfile
        )
        val saved = channelPlatformRepository.save(channelPlatform)

        // 프로필 동기화는 커밋 후 비동기로 처리한다 (외부 API 호출, 실패해도 생성은 성립)
        eventPublisher.publishEvent(ChannelPlatformCreatedEvent(saved.channel?.id!!, saved.platformType))

        val strategy = platformStrategyFactory.getPlatformStrategy(saved.platformType)
        val platformUrl = strategy.getStreamUrl(saved.platformChannelId)
        val channelProfileUrl = urlService.channelProfileUrl(saved.channel?.uuid!!)

        return ChannelPlatformAdminCreateResult.from(saved, platformUrl, channelProfileUrl)
    }

}
