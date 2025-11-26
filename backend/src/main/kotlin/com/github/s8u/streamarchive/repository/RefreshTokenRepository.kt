package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.expiresAt < :now AND rt.isActive = true")
    fun deleteExpiredTokens(now: LocalDateTime)
}