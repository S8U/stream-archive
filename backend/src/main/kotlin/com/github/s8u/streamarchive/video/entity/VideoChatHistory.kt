package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "video_chat_histories",
    indexes = [
        Index(name = "idx_video_chat_histories_video_id", columnList = "videoId"),
        Index(name = "idx_video_chat_histories_created_at", columnList = "createdAt"),
        Index(name = "idx_video_chat_histories_offset", columnList = "offsetMillis")
    ]
)
@Comment("동영상 채팅 이력")
class VideoChatHistory(
    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false, length = 255)
    @Comment("사용자명")
    val username: String,

    @Column(nullable = false, length = 1000)
    @Comment("메시지")
    val message: String,

    @Column(columnDefinition = "JSON")
    @Comment("이모지")
    val emojis: String? = null,

    @Lob
    @Column(columnDefinition = "TEXT")
    @Comment("원본 데이터")
    val data: String? = null,

    @Column(nullable = false)
    @Comment("동영상 시작 기준 오프셋 (밀리초)")
    val offsetMillis: Long,

    createdAt: LocalDateTime = LocalDateTime.now()
) : BaseTimeEntity() {

    init {
        this.createdAt = createdAt
    }

}
