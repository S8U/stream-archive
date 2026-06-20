package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRepositoryCustom {

    fun searchForAdmin(command: UserAdminSearchCommand, pageable: Pageable): Page<User>

}
