package com.github.s8u.streamarchive.user.usecase.dto.command

data class UserSignupCommand(
    val username: String,
    val name: String,
    val password: String
)
