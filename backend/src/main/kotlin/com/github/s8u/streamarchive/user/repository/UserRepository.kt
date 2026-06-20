package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {

    fun findByUsername(username: String): User?

}
