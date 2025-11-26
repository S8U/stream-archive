package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Profile("!test")
class RefreshTokenCleanupScheduler(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val logger = LoggerFactory.getLogger(RefreshTokenCleanupScheduler::class.java)

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    fun cleanupExpiredTokens() {
        logger.info("Starting expired refresh token cleanup")

        try {
            val now = LocalDateTime.now()
            refreshTokenRepository.deleteExpiredTokens(now)
            logger.info("Expired refresh token cleanup completed")
        } catch (e: Exception) {
            logger.error("Failed to cleanup expired refresh tokens", e)
        }
    }
}