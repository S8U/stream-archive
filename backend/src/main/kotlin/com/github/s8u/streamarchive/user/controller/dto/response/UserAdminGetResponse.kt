package com.github.s8u.streamarchive.user.controller.dto.response

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.usecase.dto.result.UserAdminGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "사용자 상세 조회 응답")
data class UserAdminGetResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @field:Schema(description = "사용자 UUID")
    val uuid: String,

    @field:Schema(description = "아이디", example = "user01")
    val username: String,

    @field:Schema(description = "이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "역할 (ADMIN/USER)")
    val role: Role,

    @field:Schema(description = "마지막 로그인 일시")
    val lastLoginAt: LocalDateTime?,

    @field:Schema(description = "가입 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(result: UserAdminGetResult): UserAdminGetResponse {
            return UserAdminGetResponse(
                id = result.id,
                uuid = result.uuid,
                username = result.username,
                name = result.name,
                role = result.role,
                lastLoginAt = result.lastLoginAt,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
