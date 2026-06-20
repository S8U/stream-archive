package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.result.UserAdminGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 상세 조회 (관리자)
 */
@Service
class UserAdminGetUseCase(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun get(id: Long): UserAdminGetResult {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return UserAdminGetResult.from(user)
    }

}
