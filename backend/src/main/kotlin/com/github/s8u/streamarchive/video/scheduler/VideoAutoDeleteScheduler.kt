package com.github.s8u.streamarchive.video.scheduler

import com.github.s8u.streamarchive.video.usecase.VideoAutoDeleteRunUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 동영상 자동 삭제 스케줄러
 */
@Component
@Profile("!test")
class VideoAutoDeleteScheduler(
    private val videoAutoDeleteRunUseCase: VideoAutoDeleteRunUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 만료된 동영상을 삭제한다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun deleteExpiredVideos() {
        logger.info("VideoAutoDeleteScheduler: started")

        videoAutoDeleteRunUseCase.run()

        logger.info("VideoAutoDeleteScheduler: finished")
    }

}
