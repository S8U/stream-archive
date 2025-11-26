package com.github.s8u.streamarchive.security

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.exception.BusinessException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

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
        return try {
            val claims = parseToken(token)
            claims.subject
        } catch (e: Exception) {
            logger.error("Failed to extract username from token", e)
            throw BusinessException("잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (e: Exception) {
            logger.debug("Token validation failed: {}", e.message)
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
            logger.error("Failed to parse token", e)
            throw BusinessException("잘못되거나 만료된 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }
    }
}
