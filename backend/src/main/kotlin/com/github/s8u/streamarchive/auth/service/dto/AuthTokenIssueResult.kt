package com.github.s8u.streamarchive.auth.service.dto

/**
 * 토큰 발급 결과
 */
data class AuthTokenIssueResult(
    val accessToken: String,
    val refreshToken: String
)
