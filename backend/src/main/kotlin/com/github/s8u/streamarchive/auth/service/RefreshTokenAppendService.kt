package com.github.s8u.streamarchive.auth.service

import com.github.s8u.streamarchive.auth.entity.RefreshToken
import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.auth.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.global.util.RequestUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 리프레시 토큰을 추가한다.
 *
 * 로그인·토큰 갱신이 새 토큰을 발급할 때 공유한다.
 */
@Service
class RefreshTokenAppendService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties
) {

    fun append(userId: Long, token: String) {
        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        val refreshToken = RefreshToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        ).apply {
            recordCreator(userId, RequestUtils.getClientIp())
        }
        refreshTokenRepository.save(refreshToken)
    }

}
