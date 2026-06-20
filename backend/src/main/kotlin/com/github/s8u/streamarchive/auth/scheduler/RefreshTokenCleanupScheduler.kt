package com.github.s8u.streamarchive.auth.scheduler

import com.github.s8u.streamarchive.auth.usecase.AuthTokenCleanupUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class RefreshTokenCleanupScheduler(
    private val authTokenCleanupUseCase: AuthTokenCleanupUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        logger.info("RefreshTokenCleanupScheduler: started")
        authTokenCleanupUseCase.cleanupExpiredTokens()
        logger.info("RefreshTokenCleanupScheduler: finished")
    }

}
