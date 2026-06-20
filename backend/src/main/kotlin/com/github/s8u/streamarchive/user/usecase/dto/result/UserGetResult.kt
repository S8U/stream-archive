package com.github.s8u.streamarchive.user.usecase.dto.result

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.user.entity.User

/**
 * 내 정보 조회 결과
 */
data class UserGetResult(
    val id: Long,
    val uuid: String,
    val username: String,
    val name: String,
    val role: Role
) {

    companion object {
        fun from(user: User): UserGetResult {
            return UserGetResult(
                id = user.id!!,
                uuid = user.uuid,
                username = user.username,
                name = user.name,
                role = user.role
            )
        }
    }
}
