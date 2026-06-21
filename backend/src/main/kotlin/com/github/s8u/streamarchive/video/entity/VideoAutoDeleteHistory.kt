package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "video_auto_delete_histories",
    indexes = [
        Index(name = "idx_video_auto_delete_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_auto_delete_histories_channel_id", columnList = "channelId"),
        Index(name = "idx_video_auto_delete_histories_created_at", columnList = "createdAt")
    ]
)
@Comment("동영상 자동 삭제 이력")
class VideoAutoDeleteHistory(
    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Column(nullable = false, length = 500)
    @Comment("삭제 시점 제목")
    val title: String,

    @Column(nullable = false)
    @Comment("삭제 시점 파일 크기 (바이트)")
    val fileSize: Long,

    @Column(nullable = false)
    @Comment("동영상 생성 일시")
    val videoCreatedAt: LocalDateTime
) : BaseTimeEntity()
