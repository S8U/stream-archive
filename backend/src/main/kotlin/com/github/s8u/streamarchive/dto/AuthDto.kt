package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import java.time.LocalDateTime

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserMeResponse
)

data class SignupRequest(
    val username: String,
    val name: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val email: String,
    val role: Role,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User): SignupResponse {
            return SignupResponse(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                email = user.email,
                role = user.role,
                createdAt = user.createdAt
            )
        }
    }
}

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class UserMeResponse(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val email: String,
    val role: Role
) {
    companion object {
        fun from(user: User): UserMeResponse {
            return UserMeResponse(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                email = user.email,
                role = user.role
            )
        }
    }
}
