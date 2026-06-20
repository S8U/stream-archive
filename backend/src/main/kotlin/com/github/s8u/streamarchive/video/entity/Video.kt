package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    name = "videos",
    indexes = [
        Index(name = "idx_videos_channel_id", columnList = "channelId"),
        Index(name = "idx_videos_content_privacy", columnList = "contentPrivacy"),
        Index(name = "idx_videos_created_at", columnList = "createdAt"),
        Index(name = "idx_videos_is_active", columnList = "isActive"),
        Index(name = "idx_videos_is_archived", columnList = "isArchived"),
        Index(name = "idx_videos_archived_created", columnList = "isArchived, createdAt")
    ]
)
@SQLRestriction("is_active = true")
@Comment("동영상")
class Video(
    @Column(nullable = false, unique = true, length = 36)
    @Comment("동영상 UUID")
    val uuid: String,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId", insertable = false, updatable = false)
    @Comment("채널")
    val channel: Channel? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "videoId", insertable = false, updatable = false)
    val record: Record? = null,

    title: String,
    contentPrivacy: VideoContentPrivacy,
    chatSyncOffsetMillis: Long = 0
) : BaseSoftDeleteEntity() {

    @Version
    @Column(nullable = false)
    @Comment("낙관적 락 버전")
    var version: Long = 0
        protected set

    @Column(nullable = false, length = 500)
    @Comment("제목")
    var title: String = title
        protected set

    @Column(columnDefinition = "TEXT")
    @Comment("설명")
    var description: String? = null
        protected set

    @Column(nullable = false)
    @Comment("재생 시간 (초)")
    var duration: Int = 0
        protected set

    @Column(nullable = false)
    @Comment("파일 크기 (바이트)")
    var fileSize: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("콘텐츠 공개 범위")
    var contentPrivacy: VideoContentPrivacy = contentPrivacy
        protected set

    @Column(nullable = false)
    @Comment("채팅 싱크 오프셋 (밀리초)")
    var chatSyncOffsetMillis: Long = chatSyncOffsetMillis
        protected set

    @Column(nullable = false)
    @Comment("소장 여부")
    var isArchived: Boolean = false
        protected set

    @Comment("소장 처리 일시")
    var archivedAt: LocalDateTime? = null
        protected set

    @Comment("소장 처리한 사용자 ID")
    var archivedBy: Long? = null
        protected set

    @Column(length = 45)
    @Comment("소장 처리 시 IP")
    var archivedIp: String? = null
        protected set

    /**
     * 동영상 정보를 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(title: String?, description: String?, contentPrivacy: VideoContentPrivacy?, chatSyncOffsetMillis: Long?) {
        title?.let { this.title = it }
        description?.let { this.description = it }
        contentPrivacy?.let { this.contentPrivacy = it }
        chatSyncOffsetMillis?.let { this.chatSyncOffsetMillis = it }
    }

    /**
     * 제목을 바꾼다.
     */
    fun changeTitle(title: String) {
        this.title = title
    }

    /**
     * 파일 크기와 재생 시간을 갱신한다.
     */
    fun applyMetadata(fileSize: Long, duration: Int) {
        this.fileSize = fileSize
        this.duration = duration
    }

    /**
     * 소장 처리한다.
     */
    fun archive(userId: Long?, ip: String?) {
        isArchived = true
        archivedAt = LocalDateTime.now()
        archivedBy = userId
        archivedIp = ip
    }

    /**
     * 소장을 해제한다.
     */
    fun unarchive() {
        isArchived = false
        archivedAt = null
        archivedBy = null
        archivedIp = null
    }

}
