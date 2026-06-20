package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 만료된 리프레시 토큰 정리
 */
@Service
class AuthTokenCleanupUseCase(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now())
        logger.info("AuthTokenCleanupUseCase: deleted expired refresh tokens")
    }

}
