package com.github.s8u.streamarchive.entity

import com.github.s8u.streamarchive.enums.ContentPrivacy
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "channels",
    indexes = [
        Index(name = "idx_channels_content_privacy", columnList = "content_privacy"),
        Index(name = "idx_channels_created_at", columnList = "created_at"),
        Index(name = "idx_channels_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("채널")
class Channel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("채널 ID")
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 36)
    @Comment("채널 UUID")
    val uuid: String,

    @Column(nullable = false, length = 255)
    @Comment("채널 이름")
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_privacy", nullable = false)
    @Comment("콘텐츠 공개 범위")
    var contentPrivacy: ContentPrivacy,

    @Column(name = "is_active", nullable = false)
    @Comment("활성 상태")
    var isActive: Boolean = true,

    @Column(name = "deleted_at")
    @Comment("삭제 일시")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "deleted_by")
    @Comment("삭제한 사용자 ID")
    var deletedBy: Long? = null,

    @Column(name = "deleted_ip", length = 45)
    @Comment("삭제 시 IP")
    var deletedIp: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    @Comment("생성한 사용자 ID")
    var createdBy: Long? = null,

    @Column(name = "created_ip", length = 45)
    @Comment("생성 시 IP")
    var createdIp: String? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    @Comment("수정한 사용자 ID")
    var updatedBy: Long? = null,

    @Column(name = "updated_ip", length = 45)
    @Comment("수정 시 IP")
    var updatedIp: String? = null
)