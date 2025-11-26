package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserRepository
import com.github.s8u.streamarchive.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        logger.info("Login attempt for username: {}", request.username)

        val user = userRepository.findByUsername(request.username)
            ?: throw BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)

        if (!passwordEncoder.matches(request.password, user.password)) {
            logger.warn("Login failed for username: {} - Invalid password", request.username)
            throw BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        user.lastLoginAt = LocalDateTime.now()

        val userDetails = userDetailsService.loadUserByUsername(user.username)
        val accessToken = jwtTokenProvider.generateAccessToken(userDetails)
        val refreshToken = jwtTokenProvider.generateRefreshToken(userDetails)

        logger.info("Login successful for username: {}", request.username)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserInfo.from(user)
        )
    }

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        logger.info("Signup attempt for username: {}", request.username)

        if (userRepository.findByUsername(request.username) != null) {
            throw BusinessException("이미 존재하는 아이디입니다.", HttpStatus.BAD_REQUEST)
        }

        if (userRepository.findByEmail(request.email) != null) {
            throw BusinessException("이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST)
        }

        val user = User(
            uuid = UUID.randomUUID().toString(),
            username = request.username,
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = Role.USER
        )

        val savedUser = userRepository.save(user)
        logger.info("Signup successful for username: {}", request.username)

        return SignupResponse.from(savedUser)
    }

    @Transactional(readOnly = true)
    fun refresh(request: RefreshTokenRequest): RefreshTokenResponse {
        logger.debug("Refresh token request received")

        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw BusinessException("유효하지 않거나 만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        val username = jwtTokenProvider.getUsernameFromToken(request.refreshToken)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val newAccessToken = jwtTokenProvider.generateAccessToken(userDetails)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails)

        logger.debug("Access token and refresh token refreshed for username: {}", username)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}
