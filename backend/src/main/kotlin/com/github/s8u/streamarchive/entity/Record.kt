package com.github.s8u.streamarchive.entity

import com.github.s8u.streamarchive.enums.PlatformType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "records",
    indexes = [
        Index(name = "idx_records_channel_id", columnList = "channelId"),
        Index(name = "idx_records_video_id", columnList = "videoId"),
        Index(name = "idx_records_platform_stream", columnList = "platformType,platformStreamId"),
        Index(name = "idx_records_status", columnList = "isEnded,isCancelled")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("녹화 기록")
class Record(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("녹화 기록 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    val platformType: PlatformType,

    @Column(nullable = false, length = 255)
    @Comment("플랫폼 스트림 ID")
    val platformStreamId: String,

    @Column(nullable = false, length = 50)
    @Comment("녹화 화질")
    val recordQuality: String,

    @Column(nullable = false)
    @Comment("종료 여부")
    var isEnded: Boolean = false,

    @Column(nullable = false)
    @Comment("취소 여부")
    var isCancelled: Boolean = false,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("녹화 시작 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Comment("녹화 종료 일시")
    var endedAt: LocalDateTime? = null
)