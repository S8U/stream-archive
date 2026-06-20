package com.github.s8u.streamarchive.user.usecase.dto.result

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.entity.User

/**
 * 내 정보 수정 결과
 */
data class UserUpdateResult(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val role: Role
) {

    companion object {
        fun from(user: User): UserUpdateResult {
            return UserUpdateResult(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                role = user.role
            )
        }
    }
}
