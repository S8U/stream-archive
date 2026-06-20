package com.github.s8u.streamarchive.user.usecase.dto.command

data class UserPasswordUpdateCommand(
    val currentPassword: String,
    val newPassword: String
)
