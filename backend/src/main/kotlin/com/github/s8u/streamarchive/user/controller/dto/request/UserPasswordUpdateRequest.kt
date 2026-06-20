package com.github.s8u.streamarchive.user.controller.dto.request

import com.github.s8u.streamarchive.user.usecase.dto.command.UserPasswordUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "비밀번호 변경 요청")
data class UserPasswordUpdateRequest(
    @field:Schema(description = "현재 비밀번호", example = "password1234")
    val currentPassword: String,

    @field:Schema(description = "새 비밀번호", example = "newpassword1234")
    val newPassword: String
) {

    fun toCommand(): UserPasswordUpdateCommand {
        return UserPasswordUpdateCommand(currentPassword = currentPassword, newPassword = newPassword)
    }
}
