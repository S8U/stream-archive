package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminUpdateCommand
import com.github.s8u.streamarchive.user.usecase.dto.result.UserAdminUpdateResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 수정 (관리자)
 */
@Service
class UserAdminUpdateUseCase(
    private val userRepository: UserRepository
) {

    @Transactional
    fun update(id: Long, command: UserAdminUpdateCommand): UserAdminUpdateResult {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        user.update(command.name, command.role)

        return UserAdminUpdateResult.from(user)
    }

}
