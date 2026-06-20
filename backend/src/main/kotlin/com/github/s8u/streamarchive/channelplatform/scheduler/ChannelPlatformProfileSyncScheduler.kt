package com.github.s8u.streamarchive.channelplatform.scheduler

import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformProfileSyncAllUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 채널 플랫폼 프로필 동기화 스케줄러
 */
@Component
@Profile("!test")
class ChannelPlatformProfileSyncScheduler(
    private val channelPlatformProfileSyncAllUseCase: ChannelPlatformProfileSyncAllUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 0 * * *")
    fun syncAllChannelProfiles() {
        logger.info("ChannelPlatformProfileSyncScheduler: Starting scheduled channel profile sync")

        channelPlatformProfileSyncAllUseCase.syncAll()

        logger.info("ChannelPlatformProfileSyncScheduler: Completed scheduled channel profile sync")
    }

}
