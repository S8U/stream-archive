package com.github.s8u.streamarchive.auth.usecase.dto.command

data class AuthLoginCommand(
    val username: String,
    val password: String
)
