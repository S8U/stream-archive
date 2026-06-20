package com.github.s8u.streamarchive.auth.usecase

import com.github.s8u.streamarchive.auth.service.AuthTokenIssueService
import com.github.s8u.streamarchive.auth.usecase.dto.command.AuthLoginCommand
import com.github.s8u.streamarchive.auth.usecase.dto.result.AuthLoginResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그인
 *
 * 비밀번호를 검증하고 토큰을 발급한다.
 * 마지막 로그인 일시를 갱신하고 리프레시 토큰을 저장한다.
 */
@Service
class AuthLoginUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssueService: AuthTokenIssueService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun login(command: AuthLoginCommand): AuthLoginResult {
        val user = userRepository.findByUsername(command.username)
            ?: throw BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)

        if (!passwordEncoder.matches(command.password, user.password)) {
            logger.warn("AuthLoginUseCase: login failed for username={}", command.username)
            throw BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        user.login()

        val tokens = authTokenIssueService.issue(user.id!!, user.username)

        logger.info("AuthLoginUseCase: login success for username={}", command.username)

        return AuthLoginResult(tokens.accessToken, tokens.refreshToken)
    }

}
