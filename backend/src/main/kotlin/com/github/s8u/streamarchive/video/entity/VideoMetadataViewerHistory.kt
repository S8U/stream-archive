package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "video_metadata_viewer_histories",
    indexes = [
        Index(name = "idx_video_metadata_viewer_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_metadata_viewer_histories_created_at", columnList = "createdAt"),
        Index(name = "idx_video_metadata_viewer_histories_offset", columnList = "offsetMillis")
    ]
)
@Comment("동영상 시청자 수 이력")
class VideoMetadataViewerHistory(
    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("시청자 수")
    val viewerCount: Int,

    @Column(nullable = false)
    @Comment("동영상 시작 기준 오프셋 (밀리초)")
    val offsetMillis: Long
) : BaseTimeEntity()
