package com.github.s8u.streamarchive.security

import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED)

        return User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            .build()
    }
}
