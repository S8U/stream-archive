package com.github.s8u.streamarchive.entity

import com.github.s8u.streamarchive.enums.Role
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_username", columnList = "username"),
        Index(name = "idx_users_role", columnList = "role"),
        Index(name = "idx_users_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("is_active = true")
@Comment("사용자")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자 ID")
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 36)
    @Comment("사용자 UUID")
    val uuid: String,

    @Column(nullable = false, unique = true, length = 100)
    @Comment("사용자명")
    val username: String,

    @Column(nullable = false, length = 100)
    @Comment("이름")
    var name: String,

    @Column(nullable = false, length = 255)
    @Comment("비밀번호")
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("역할")
    var role: Role = Role.USER,

    @Column
    @Comment("마지막 로그인 일시")
    var lastLoginAt: LocalDateTime? = null,

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
    var createdIp: String? = null,

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    @Comment("수정한 사용자 ID")
    var updatedBy: Long? = null,

    @Column(length = 45)
    @Comment("수정 시 IP")
    var updatedIp: String? = null
)