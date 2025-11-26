package com.github.s8u.streamarchive.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
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
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("is_active = true")
@Comment("리프레시 토큰")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("리프레시 토큰 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("사용자 ID")
    val userId: Long,

    @Column(nullable = false, unique = true, length = 500)
    @Comment("토큰")
    val token: String,

    @Column(nullable = false)
    @Comment("만료 일시")
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    @Comment("활성 상태")
    var isActive: Boolean = true,

    @Column
    @Comment("삭제 일시")
    var deletedAt: LocalDateTime? = null,

    @Column
    @Comment("삭제한 사용자 ID")
    var deletedBy: Long? = null,

    @Column(length = 45)
    @Comment("삭제 시 IP")
    var deletedIp: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    @Comment("생성한 사용자 ID")
    var createdBy: Long? = null,

    @Column(length = 45)
    @Comment("생성 시 IP")
    var createdIp: String? = null
)