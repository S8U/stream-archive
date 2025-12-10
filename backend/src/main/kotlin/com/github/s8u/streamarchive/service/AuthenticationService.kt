package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository
) {
    fun getCurrentUserId(): Long? {
        val user = getCurrentUser() ?: return null
        return user.id
    }

    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (!authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return null
        }
        
        val username = authentication.name ?: return null
        return userRepository.findByUsername(username)
    }

    fun isAuthenticated(): Boolean {
        return getCurrentUserId() != null
    }

    fun isAdmin(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        if (!authentication.isAuthenticated) {
            return false
        }
        return authentication.authorities.any { it.authority == "ROLE_ADMIN" }
    }
}
