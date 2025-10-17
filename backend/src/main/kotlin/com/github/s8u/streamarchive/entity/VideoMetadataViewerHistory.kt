package com.github.s8u.streamarchive.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "video_metadata_viewer_histories",
    indexes = [
        Index(name = "idx_video_metadata_viewer_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_metadata_viewer_histories_created_at", columnList = "createdAt"),
        Index(name = "idx_video_metadata_viewer_histories_offset", columnList = "offsetMillis")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("동영상 시청자 수 이력")
class VideoMetadataViewerHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("시청자 수 이력 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("시청자 수")
    val viewerCount: Int,

    @Column(nullable = false)
    @Comment("동영상 시작 기준 오프셋 (밀리초)")
    val offsetMillis: Long,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now()
)