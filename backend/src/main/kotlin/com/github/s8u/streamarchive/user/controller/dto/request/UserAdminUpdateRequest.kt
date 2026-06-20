package com.github.s8u.streamarchive.user.controller.dto.request

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 수정 요청")
data class UserAdminUpdateRequest(
    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null,

    @field:Schema(description = "역할 (ADMIN/USER)")
    val role: Role? = null
) {

    fun toCommand(): UserAdminUpdateCommand {
        return UserAdminUpdateCommand(name = name, role = role)
    }
}
