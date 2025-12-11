package com.github.s8u.streamarchive.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class SignupRequest(
    val username: String,
    val name: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String? = null
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String? = null
)