package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.user.enums.Role
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.repository.UserRepository
import com.github.s8u.streamarchive.user.usecase.dto.command.UserSignupCommand
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 회원가입
 *
 * 아이디 중복을 검사하고 사용자를 생성한다.
 */
@Service
class UserSignupUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun signup(command: UserSignupCommand) {
        if (userRepository.findByUsername(command.username) != null) {
            throw BusinessException("이미 존재하는 아이디입니다.", HttpStatus.BAD_REQUEST)
        }

        val user = User(
            uuid = UUID.randomUUID().toString(),
            username = command.username,
            name = command.name,
            password = passwordEncoder.encode(command.password),
            role = Role.USER
        )
        userRepository.save(user)

        logger.info("UserSignupUseCase: signup success for username={}", command.username)
    }

}
