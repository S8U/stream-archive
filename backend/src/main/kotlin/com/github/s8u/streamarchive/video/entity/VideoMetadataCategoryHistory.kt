package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "video_metadata_category_histories",
    indexes = [
        Index(name = "idx_video_metadata_category_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_metadata_category_histories_created_at", columnList = "createdAt"),
        Index(name = "idx_video_metadata_category_histories_offset", columnList = "offsetMillis")
    ]
)
@Comment("동영상 카테고리 변경 이력")
class VideoMetadataCategoryHistory(
    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(length = 255)
    @Comment("카테고리")
    val category: String? = null,

    @Column(nullable = false)
    @Comment("동영상 시작 기준 오프셋 (밀리초)")
    val offsetMillis: Long
) : BaseTimeEntity()
