package com.github.s8u.streamarchive.record.entity

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.global.entity.base.BaseTimeEntity
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.entity.Video
import jakarta.persistence.*
import org.hibernate.annotations.Comment
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
@Comment("녹화 기록")
class Record(
    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId", insertable = false, updatable = false)
    @Comment("채널")
    val channel: Channel? = null,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoId", insertable = false, updatable = false)
    @Comment("동영상")
    val video: Video? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    val platformType: PlatformType,

    @Column(nullable = false, length = 255)
    @Comment("플랫폼 스트림 ID")
    val platformStreamId: String,

    @Column(nullable = false, length = 50)
    @Comment("녹화 화질")
    val recordQuality: String
) : BaseTimeEntity() {

    @Column(nullable = false)
    @Comment("종료 여부")
    var isEnded: Boolean = false
        protected set

    @Column(nullable = false)
    @Comment("취소 여부")
    var isCancelled: Boolean = false
        protected set

    @Column(nullable = false)
    @Comment("녹화 시작 실패 여부")
    var isFailed: Boolean = false
        protected set

    @Column
    @Comment("녹화 종료 일시")
    var endedAt: LocalDateTime? = null
        protected set

    /**
     * 녹화를 종료한다.
     *
     * [isCancelled]는 사용자의 수동 취소 여부다.
     */
    fun end(isCancelled: Boolean) {
        isEnded = true
        this.isCancelled = isCancelled
        endedAt = LocalDateTime.now()
    }

    /**
     * 녹화 프로세스 시작에 실패해 종료 처리한다.
     */
    fun failToStart() {
        isEnded = true
        isFailed = true
        endedAt = LocalDateTime.now()
    }

    /**
     * 시작 실패로 표시한다.
     *
     * 이미 종료된 녹화를 재녹화 폭주 방지 카운트에 포함시키기 위함이다.
     */
    fun markFailed() {
        isFailed = true
    }

}
