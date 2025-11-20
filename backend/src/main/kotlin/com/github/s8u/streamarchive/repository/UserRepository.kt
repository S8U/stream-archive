package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom
