package com.github.s8u.streamarchive.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "video_watch_histories",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_video", columnNames = ["userId", "videoId"])
    ],
    indexes = [
        Index(name = "idx_user_watched", columnList = "userId,watchedAt")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("동영상 시청 기록")
class VideoWatchHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("시청 기록 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("사용자 ID")
    val userId: Long,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("마지막 재생 위치 (초)")
    var lastPosition: Int,

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("시청 일시")
    var watchedAt: LocalDateTime = LocalDateTime.now()
)