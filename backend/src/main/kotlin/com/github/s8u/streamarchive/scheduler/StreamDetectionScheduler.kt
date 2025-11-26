package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class StreamDetectionScheduler(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(StreamDetectionScheduler::class.java)

    @Scheduled(fixedDelay = 10000)
    fun detectStreams() {
        logger.debug("Starting stream detection")

        val channelPlatforms = channelPlatformRepository.findAll()

        channelPlatforms.forEach { channelPlatform ->
            try {
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val stream = strategy.getStream(channelPlatform.platformChannelId)

                if (stream != null) {
                    logger.debug(
                        "Stream detected: channelId={}, platformType={}, streamId={}",
                        channelPlatform.channelId,
                        channelPlatform.platformType,
                        stream.id
                    )
                    eventPublisher.publishEvent(StreamDetectedEvent(channelPlatform, stream))
                }
            } catch (e: Exception) {
                logger.error(
                    "Failed to detect stream: channelId={}, platformType={}",
                    channelPlatform.channelId,
                    channelPlatform.platformType,
                    e
                )
            }
        }

        logger.debug("Finished stream detection")
    }

}
