package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminUserResponse
import com.github.s8u.streamarchive.dto.AdminUserSearchRequest
import com.github.s8u.streamarchive.dto.AdminUserUpdateRequest
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminUserSearchRequest, pageable: Pageable): Page<AdminUserResponse> {
        return userRepository.searchForAdmin(request, pageable)
            .map { AdminUserResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminUserResponse {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminUserResponse.from(user)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminUserUpdateRequest): AdminUserResponse {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.name?.let { user.name = it }
        request.email?.let { user.email = it }
        request.role?.let { user.role = it }

        return AdminUserResponse.from(user)
    }

    @Transactional
    fun delete(id: Long) {
        val user = userRepository.findById(id).orElseThrow {
            BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        user.isActive = false
        user.deletedAt = LocalDateTime.now()
    }
}
