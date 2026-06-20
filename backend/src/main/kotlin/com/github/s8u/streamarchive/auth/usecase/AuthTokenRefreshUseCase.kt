package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.jwt.service.JwtTokenService
import com.github.s8u.streamarchive.auth.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.auth.service.AuthTokenIssueService
import com.github.s8u.streamarchive.auth.usecase.dto.result.AuthTokenRefreshResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.util.RequestUtils
import com.github.s8u.streamarchive.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 토큰 갱신
 *
 * 리프레시 토큰을 검증하고 기존 토큰을 무효화한다.
 * 새 토큰 쌍을 발급한다.
 */
@Service
class AuthTokenRefreshUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService,
    private val authTokenIssueService: AuthTokenIssueService
) {

    @Transactional
    fun refresh(refreshToken: String): AuthTokenRefreshResult {
        if (!jwtTokenService.validateToken(refreshToken)) {
            throw BusinessException("유효하지 않거나 만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        val storedToken = refreshTokenRepository.findByToken(refreshToken)
            ?: throw BusinessException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)

        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw BusinessException("만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        val username = jwtTokenService.getUsernameFromToken(refreshToken)
        val user = userRepository.findByUsername(username)
            ?: throw BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED)

        // 기존 리프레시 토큰 무효화
        storedToken.softDelete(storedToken.userId, RequestUtils.getClientIp())

        val tokens = authTokenIssueService.issue(user.id!!, username)

        return AuthTokenRefreshResult(tokens.accessToken, tokens.refreshToken)
    }

}
