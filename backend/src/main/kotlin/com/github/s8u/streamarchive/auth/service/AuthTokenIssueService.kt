package com.github.s8u.streamarchive.auth.service

import com.github.s8u.streamarchive.auth.jwt.service.JwtTokenService
import com.github.s8u.streamarchive.auth.service.dto.AuthTokenIssueResult
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

/**
 * 토큰 쌍을 발급한다.
 *
 * 액세스·리프레시 토큰을 만들고 리프레시 토큰을 저장한다.
 * 로그인·토큰 갱신이 공유한다.
 */
@Service
class AuthTokenIssueService(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val refreshTokenAppendService: RefreshTokenAppendService
) {

    fun issue(userId: Long, username: String): AuthTokenIssueResult {
        val userDetails = userDetailsService.loadUserByUsername(username)
        val accessToken = jwtTokenService.generateAccessToken(userDetails)
        val refreshToken = jwtTokenService.generateRefreshToken(userDetails)
        refreshTokenAppendService.append(userId, refreshToken)

        return AuthTokenIssueResult(accessToken, refreshToken)
    }

}
