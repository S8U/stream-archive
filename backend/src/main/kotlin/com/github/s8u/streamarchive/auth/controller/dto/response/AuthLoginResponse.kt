package com.github.s8u.streamarchive.auth.controller.dto.response

import com.github.s8u.streamarchive.auth.usecase.dto.result.AuthLoginResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 응답")
data class AuthLoginResponse(
    @field:Schema(description = "액세스 토큰")
    val accessToken: String,

    @field:Schema(description = "리프레시 토큰")
    val refreshToken: String
) {

    companion object {
        fun from(result: AuthLoginResult): AuthLoginResponse {
            return AuthLoginResponse(accessToken = result.accessToken, refreshToken = result.refreshToken)
        }
    }
}
