package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 삭제 (관리자)
 */
@Service
class UserAdminDeleteUseCase(
    private val userRepository: UserRepository
) {

    @Transactional
    fun delete(id: Long) {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        user.softDelete(null, null)
    }

}
