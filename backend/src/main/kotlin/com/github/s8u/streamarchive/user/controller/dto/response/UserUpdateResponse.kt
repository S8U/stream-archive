package com.github.s8u.streamarchive.user.controller.dto.response

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.usecase.dto.result.UserUpdateResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 정보 수정 응답")
data class UserUpdateResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @field:Schema(description = "사용자 UUID")
    val uuid: String,

    @field:Schema(description = "아이디", example = "user01")
    val username: String,

    @field:Schema(description = "이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "역할 (ADMIN/USER)")
    val role: Role
) {

    companion object {
        fun from(result: UserUpdateResult): UserUpdateResponse {
            return UserUpdateResponse(
                id = result.id,
                uuid = result.uuid,
                username = result.username,
                name = result.name,
                role = result.role
            )
        }
    }
}
