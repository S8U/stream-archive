package com.github.s8u.streamarchive.auth.controller.dto.request

import com.github.s8u.streamarchive.auth.usecase.dto.command.AuthLoginCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 요청")
data class AuthLoginRequest(
    @field:Schema(description = "아이디", example = "user01")
    val username: String,

    @field:Schema(description = "비밀번호", example = "password1234")
    val password: String
) {

    fun toCommand(): AuthLoginCommand {
        return AuthLoginCommand(username = username, password = password)
    }
}
