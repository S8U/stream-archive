package com.github.s8u.streamarchive.detect.scheduler

import com.github.s8u.streamarchive.detect.usecase.StreamDetectUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 스트리밍 감지 스케줄러
 */
@Component
@Profile("!test")
class StreamDetectScheduler(
    private val streamDetectUseCase: StreamDetectUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10000)
    fun detectStreams() {
        logger.debug("StreamDetectScheduler: Starting stream detection")

        streamDetectUseCase.detect()

        logger.debug("StreamDetectScheduler: Finished stream detection")
    }
}
