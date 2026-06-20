package com.github.s8u.streamarchive.auth.entity

import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_token", columnList = "token"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
        Index(name = "idx_refresh_tokens_is_active", columnList = "is_active")
    ]
)
@SQLRestriction("is_active = true")
@Comment("리프레시 토큰")
class RefreshToken(
    @Column(nullable = false)
    @Comment("사용자 ID")
    val userId: Long,

    @Column(nullable = false, unique = true, length = 500)
    @Comment("토큰")
    val token: String,

    @Column(nullable = false)
    @Comment("만료 일시")
    val expiresAt: LocalDateTime
) : BaseSoftDeleteEntity()
