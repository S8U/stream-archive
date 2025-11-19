package com.github.s8u.streamarchive.entity

import com.github.s8u.streamarchive.enums.PlatformType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "channel_platforms",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_channel_platforms", columnNames = ["channel_id", "platform_type"])
    ],
    indexes = [
        Index(name = "idx_channel_platforms_platform_type", columnList = "platform_type"),
        Index(name = "idx_channel_platforms_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("is_active = true")
@Comment("채널-플랫폼 연동")
class ChannelPlatform(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("채널-플랫폼 연동 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    val platformType: PlatformType,

    @Column(nullable = false, length = 255)
    @Comment("플랫폼 채널 ID")
    val platformChannelId: String,

    @Column(nullable = false)
    @Comment("프로필 동기화 여부")
    var isSyncProfile: Boolean = true,

    @Column(nullable = false)
    @Comment("활성 상태")
    var isActive: Boolean = true,

    @Comment("삭제 일시")
    var deletedAt: LocalDateTime? = null,

    @Comment("삭제한 사용자 ID")
    var deletedBy: Long? = null,

    @Column(length = 45)
    @Comment("삭제 시 IP")
    var deletedIp: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Comment("생성한 사용자 ID")
    var createdBy: Long? = null,

    @Column(length = 45)
    @Comment("생성 시 IP")
    var createdIp: String? = null,

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Comment("수정한 사용자 ID")
    var updatedBy: Long? = null,

    @Column(length = 45)
    @Comment("수정 시 IP")
    var updatedIp: String? = null
)