package com.github.s8u.streamarchive.user.usecase.dto.result

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.entity.User
import java.time.LocalDateTime

/**
 * 사용자 목록 조회 결과 (관리자)
 */
data class UserAdminSearchResult(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val role: Role,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(user: User): UserAdminSearchResult {
            return UserAdminSearchResult(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                role = user.role,
                lastLoginAt = user.lastLoginAt,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
