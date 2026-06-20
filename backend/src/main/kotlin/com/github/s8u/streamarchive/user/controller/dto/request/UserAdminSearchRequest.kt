package com.github.s8u.streamarchive.user.controller.dto.request

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminSearchCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "사용자 검색 요청")
data class UserAdminSearchRequest(
    @field:Schema(description = "사용자 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "아이디", example = "user01")
    val username: String? = null,

    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null,

    @field:Schema(description = "역할 (ADMIN/USER)")
    val role: Role? = null,

    @field:Schema(description = "가입 일시 시작")
    val createdAtFrom: LocalDateTime? = null,

    @field:Schema(description = "가입 일시 끝")
    val createdAtTo: LocalDateTime? = null
) {

    fun toCommand(): UserAdminSearchCommand {
        return UserAdminSearchCommand(
            id = id,
            username = username,
            name = name,
            role = role,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo
        )
    }
}
