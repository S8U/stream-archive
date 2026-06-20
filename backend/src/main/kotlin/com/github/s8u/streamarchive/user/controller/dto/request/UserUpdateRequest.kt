package com.github.s8u.streamarchive.user.controller.dto.request

import com.github.s8u.streamarchive.user.usecase.dto.command.UserUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 정보 수정 요청")
data class UserUpdateRequest(
    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null
) {

    fun toCommand(): UserUpdateCommand {
        return UserUpdateCommand(name = name)
    }
}
