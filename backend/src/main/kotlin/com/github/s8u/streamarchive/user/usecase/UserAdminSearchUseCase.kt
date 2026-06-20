package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminSearchCommand
import com.github.s8u.streamarchive.user.usecase.dto.result.UserAdminSearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 목록 조회 (관리자)
 */
@Service
class UserAdminSearchUseCase(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun search(command: UserAdminSearchCommand, pageable: Pageable): Page<UserAdminSearchResult> {
        return userRepository.searchForAdmin(command, pageable)
            .map { UserAdminSearchResult.from(it) }
    }

}
