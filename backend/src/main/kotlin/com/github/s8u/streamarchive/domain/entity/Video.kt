package com.github.s8u.streamarchive.domain.entity

import com.github.s8u.streamarchive.domain.enums.ContentPrivacy
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "videos",
    indexes = [
        Index(name = "idx_videos_channel_id", columnList = "channelId"),
        Index(name = "idx_videos_content_privacy", columnList = "contentPrivacy"),
        Index(name = "idx_videos_created_at", columnList = "createdAt"),
        Index(name = "idx_videos_is_active", columnList = "isActive")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("동영상")
class Video(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("동영상 ID")
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 36)
    @Comment("동영상 UUID")
    val uuid: String,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Column(nullable = false, length = 500)
    @Comment("제목")
    var title: String,

    @Column(nullable = false)
    @Comment("재생 시간 (초)")
    var duration: Int = 0,

    @Column(nullable = false)
    @Comment("파일 크기 (바이트)")
    var fileSize: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("콘텐츠 공개 범위")
    var contentPrivacy: ContentPrivacy,

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