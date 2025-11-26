package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.service.ChannelProfileService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class ChannelProfileSyncScheduler(
    private val channelProfileService: ChannelProfileService
) {
    private val logger = LoggerFactory.getLogger(ChannelProfileSyncScheduler::class.java)

    @Scheduled(cron = "0 0 0 * * *")
    fun syncAllChannelProfiles() {
        logger.info("Starting scheduled channel profile sync")

        channelProfileService.syncAllProfiles()

        logger.info("Completed scheduled channel profile sync")
    }
}
