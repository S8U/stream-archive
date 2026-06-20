package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.global.util.RequestUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그아웃
 *
 * 저장된 리프레시 토큰을 소프트 삭제 처리한다.
 */
@Service
class AuthLogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun logout(refreshToken: String) {
        val storedToken = refreshTokenRepository.findByToken(refreshToken) ?: return
        storedToken.softDelete(storedToken.userId, RequestUtils.getClientIp())
    }

}
