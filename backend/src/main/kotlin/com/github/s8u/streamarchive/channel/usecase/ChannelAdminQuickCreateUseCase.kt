package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminQuickCreateCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminQuickCreateResult
import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.event.ChannelPlatformCreatedEvent
import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 간편 채널 생성 (관리자)
 *
 * 채널 생성, 플랫폼 연동, 녹화 스케줄을 한 번에 만든다.
 * 프로필 동기화 실패는 생성 결과에 영향을 주지 않는다.
 */
@Service
class ChannelAdminQuickCreateUseCase(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val recordScheduleRepository: RecordScheduleRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlService: UrlService,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun create(command: ChannelAdminQuickCreateCommand): ChannelAdminQuickCreateResult {
        // 이미 등록된 채널 플랫폼이면 거절
        if (channelPlatformRepository.existsByPlatformTypeAndPlatformChannelId(command.platformType, command.platformChannelId)) {
            throw BusinessException("이미 등록된 채널 플랫폼입니다.", HttpStatus.CONFLICT)
        }

        // 채널 생성
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = command.name,
            contentPrivacy = command.contentPrivacy
        )
        val savedChannel = channelRepository.save(channel)

        // 플랫폼 연동 생성
        val channelPlatform = ChannelPlatform(
            channel = savedChannel,
            platformType = command.platformType,
            platformChannelId = command.platformChannelId,
            isSyncProfile = command.isSyncProfile
        )
        val savedChannelPlatform = channelPlatformRepository.save(channelPlatform)

        // 녹화 스케줄 생성 (요청에 있을 때만)
        val savedRecordSchedule = command.schedule?.let { schedule ->
            recordScheduleRepository.save(
                RecordSchedule(
                    channel = savedChannel,
                    platformType = command.platformType,
                    scheduleType = schedule.scheduleType,
                    value = schedule.value,
                    recordQuality = schedule.recordQuality,
                    priority = schedule.priority,
                    autoArchive = schedule.autoArchive
                )
            )
        }

        // 프로필 동기화는 커밋 후 비동기로 처리한다
        // 외부 API 호출이 실패해도 생성은 성립한다
        eventPublisher.publishEvent(ChannelPlatformCreatedEvent(savedChannel.id!!, command.platformType))

        logger.info(
            "ChannelAdminQuickCreateUseCase: created channel " +
                "channelId=${savedChannel.id}, platformType=${command.platformType}, " +
                "platformChannelId=${command.platformChannelId}, scheduleId=${savedRecordSchedule?.id}"
        )

        val strategy = platformStrategyFactory.getPlatformStrategy(command.platformType)

        return ChannelAdminQuickCreateResult.from(
            channel = savedChannel,
            channelPlatform = savedChannelPlatform,
            recordSchedule = savedRecordSchedule,
            profileUrl = urlService.channelProfileUrl(savedChannel.uuid),
            platformUrl = strategy.getStreamUrl(command.platformChannelId)
        )
    }

}
