package com.github.s8u.streamarchive.auth.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 갱신 요청")
data class AuthTokenRefreshRequest(
    @field:Schema(description = "리프레시 토큰 (쿠키에 없을 때만 사용)")
    val refreshToken: String? = null
)
