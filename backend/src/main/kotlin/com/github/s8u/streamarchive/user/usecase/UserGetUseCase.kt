package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.usecase.dto.result.UserGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 정보 조회
 */
@Service
class UserGetUseCase(
    private val currentUserService: CurrentUserService
) {

    @Transactional(readOnly = true)
    fun get(): UserGetResult {
        val user = currentUserService.getCurrentUser()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        return UserGetResult.from(user)
    }

}
