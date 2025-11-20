package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminUserSearchRequest
import com.github.s8u.streamarchive.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRepositoryCustom {
    fun search(request: AdminUserSearchRequest, pageable: Pageable): Page<User>
}
