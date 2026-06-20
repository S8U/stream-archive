package com.github.s8u.streamarchive.auth.controller.dto.request

import com.github.s8u.streamarchive.user.usecase.dto.command.UserSignupCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 요청")
data class UserSignupRequest(
    @field:Schema(description = "아이디", example = "user01")
    val username: String,

    @field:Schema(description = "이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "비밀번호", example = "password1234")
    val password: String
) {

    fun toCommand(): UserSignupCommand {
        return UserSignupCommand(username = username, name = name, password = password)
    }
}
