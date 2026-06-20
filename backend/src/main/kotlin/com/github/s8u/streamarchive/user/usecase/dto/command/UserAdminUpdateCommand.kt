package com.github.s8u.streamarchive.user.usecase.dto.command

import com.github.s8u.streamarchive.user.enums.Role

data class UserAdminUpdateCommand(
    val name: String? = null,
    val role: Role? = null
)
