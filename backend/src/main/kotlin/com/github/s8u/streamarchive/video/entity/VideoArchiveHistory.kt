package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "video_archive_histories",
    indexes = [
        Index(name = "idx_video_archive_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_archive_histories_created_at", columnList = "createdAt")
    ]
)
@Comment("동영상 소장 이력")
class VideoArchiveHistory(
    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("소장 여부 (해당 시점 전환값)")
    val isArchived: Boolean,

    @Column
    @Comment("액션 수행 사용자 ID")
    val actionBy: Long? = null,

    @Column(length = 45)
    @Comment("액션 수행 시 IP")
    val actionIp: String? = null
) : BaseTimeEntity()
