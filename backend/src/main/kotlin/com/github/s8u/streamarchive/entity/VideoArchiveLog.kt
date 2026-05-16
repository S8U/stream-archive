package com.github.s8u.streamarchive.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "video_archive_logs",
    indexes = [
        Index(name = "idx_video_archive_logs_video_id", columnList = "videoId"),
        Index(name = "idx_video_archive_logs_created_at", columnList = "createdAt")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("동영상 소장 이력")
class VideoArchiveLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("동영상 소장 이력 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("소장 여부 (해당 시점 전환값)")
    val isArchived: Boolean,

    @Comment("액션 수행 사용자 ID")
    val actionBy: Long? = null,

    @Column(length = 45)
    @Comment("액션 수행 시 IP")
    val actionIp: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
