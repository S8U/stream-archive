package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserUpdateCommand
import com.github.s8u.streamarchive.user.usecase.dto.result.UserUpdateResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 정보 수정
 */
@Service
class UserUpdateUseCase(
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
) {

    @Transactional
    fun update(command: UserUpdateCommand): UserUpdateResult {
        val user = currentUserService.getCurrentUser()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        command.name?.let { user.updateName(it) }

        return UserUpdateResult.from(userRepository.save(user))
    }

}
