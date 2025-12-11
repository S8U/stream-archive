package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import java.time.LocalDateTime

data class AdminUserUpdateRequest(
    val name: String?,
    val role: Role?
)

data class AdminUserSearchRequest(
    val id: Long? = null,
    val username: String? = null,
    val name: String? = null,
    val role: Role? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)

data class AdminUserResponse(
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
        fun from(user: User): AdminUserResponse {
            return AdminUserResponse(
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

data class UserUpdateRequest(
    val name: String? = null
)

data class UserUpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UserResponse(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val role: Role
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                role = user.role
            )
        }
    }
}