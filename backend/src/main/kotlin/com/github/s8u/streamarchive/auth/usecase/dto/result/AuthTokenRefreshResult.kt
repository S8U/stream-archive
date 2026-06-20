package com.github.s8u.streamarchive.auth.usecase.dto.result

/**
 * 토큰 갱신 결과
 */
data class AuthTokenRefreshResult(
    val accessToken: String,
    val refreshToken: String
)
