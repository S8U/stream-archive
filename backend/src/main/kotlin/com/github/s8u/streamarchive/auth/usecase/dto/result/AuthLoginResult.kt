package com.github.s8u.streamarchive.auth.usecase.dto.result

/**
 * 로그인 결과
 */
data class AuthLoginResult(
    val accessToken: String,
    val refreshToken: String
)
