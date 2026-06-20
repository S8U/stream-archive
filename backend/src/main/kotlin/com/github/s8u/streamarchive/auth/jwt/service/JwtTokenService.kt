package com.github.s8u.streamarchive.auth.jwt.service

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.global.exception.BusinessException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtTokenService(
    private val jwtProperties: JwtProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(userDetails, jwtProperties.accessTokenExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, jwtProperties.refreshTokenExpiration)
    }

    private fun generateToken(userDetails: UserDetails, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        return parseToken(token).subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (e: Exception) {
            logger.debug("JwtTokenService: Token validation failed: {}", e.message)
            false
        }
    }

    private fun parseToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            // 잘못된·만료된 토큰은 클라이언트 잘못(401)이므로 WARN으로 남긴다
            logger.warn("JwtTokenService: Failed to parse token: {}", e.message)
            throw BusinessException("잘못되거나 만료된 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }
    }

}
