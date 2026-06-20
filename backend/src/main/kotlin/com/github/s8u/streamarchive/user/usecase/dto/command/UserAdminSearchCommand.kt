package com.github.s8u.streamarchive.user.usecase.dto.command

import com.github.s8u.streamarchive.user.enums.Role
import java.time.LocalDateTime

data class UserAdminSearchCommand(
    val id: Long? = null,
    val username: String? = null,
    val name: String? = null,
    val role: Role? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)
