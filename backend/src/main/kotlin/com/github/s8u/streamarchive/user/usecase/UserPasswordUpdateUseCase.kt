package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserPasswordUpdateCommand
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 비밀번호 변경
 *
 * 현재 비밀번호를 검증하고 새 비밀번호로 변경한다.
 */
@Service
class UserPasswordUpdateUseCase(
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun update(command: UserPasswordUpdateCommand) {
        val user = currentUserService.getCurrentUser()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        if (!passwordEncoder.matches(command.currentPassword, user.password)) {
            throw BusinessException("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        user.changePassword(passwordEncoder.encode(command.newPassword))
        userRepository.save(user)
    }

}
