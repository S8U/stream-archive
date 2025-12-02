package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.entity.RefreshToken
import com.github.s8u.streamarchive.entity.User
import com.github.s8u.streamarchive.enums.Role
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.RefreshTokenRepository
import com.github.s8u.streamarchive.repository.UserRepository
import com.github.s8u.streamarchive.security.JwtTokenProvider
import com.github.s8u.streamarchive.util.RequestUtils
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
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
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

        // 새로운 리프레시 토큰 DB에 저장
        val clientIp = RequestUtils.getClientIp()
        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id!!,
                token = refreshToken,
                expiresAt = expiresAt,
                createdBy = user.id,
                createdIp = clientIp
            )
        )

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

    @Transactional
    fun refresh(refreshToken: String): RefreshTokenResponse {
        logger.debug("Refresh token request received")

        // JWT 토큰 자체의 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException("유효하지 않거나 만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        // DB에서 리프레시 토큰 조회
        val storedToken = refreshTokenRepository.findByToken(refreshToken)
            ?: throw BusinessException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)

        // 만료 시간 확인
        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw BusinessException("만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        val username = jwtTokenProvider.getUsernameFromToken(refreshToken)
        val user = userRepository.findByUsername(username)
            ?: throw BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED)

        // 기존 리프레시 토큰 무효화
        storedToken.isActive = false

        val userDetails = userDetailsService.loadUserByUsername(username)
        val newAccessToken = jwtTokenProvider.generateAccessToken(userDetails)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails)

        // 새로운 리프레시 토큰 DB에 저장
        val clientIp = RequestUtils.getClientIp()
        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id!!,
                token = newRefreshToken,
                expiresAt = expiresAt,
                createdBy = user.id,
                createdIp = clientIp
            )
        )

        logger.debug("Access token and refresh token refreshed for username: {}", username)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun logout(refreshToken: String) {
        logger.debug("Logout request received")

        // DB에서 리프레시 토큰 조회 및 무효화
        val storedToken = refreshTokenRepository.findByToken(refreshToken)
        if (storedToken != null) {
            storedToken.isActive = false
            storedToken.deletedAt = LocalDateTime.now()
            storedToken.deletedBy = storedToken.userId
            storedToken.deletedIp = RequestUtils.getClientIp()
            logger.debug("Refresh token revoked for userId: {}", storedToken.userId)
        }
    }
}
