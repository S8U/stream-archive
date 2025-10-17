package com.github.s8u.streamarchive.entity

import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordScheduleType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "record_schedules",
    indexes = [
        Index(name = "idx_record_schedules_channel_platform", columnList = "channelId,platformType"),
        Index(name = "idx_record_schedules_type", columnList = "recordScheduleType"),
        Index(name = "idx_record_schedules_is_active", columnList = "isActive")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("녹화 스케줄")
class RecordSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("녹화 스케줄 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    val platformType: PlatformType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("녹화 스케줄 유형")
    val scheduleType: RecordScheduleType,

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("스케줄 값")
    var value: String,

    @Column(nullable = false)
    @Comment("활성 상태")
    var isActive: Boolean = true,

    @Comment("삭제 일시")
    var deletedAt: LocalDateTime? = null,

    @Comment("삭제한 사용자 ID")
    var deletedBy: Long? = null,

    @Column(length = 45)
    @Comment("삭제 시 IP")
    var deletedIp: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Comment("생성한 사용자 ID")
    var createdBy: Long? = null,

    @Column(length = 45)
    @Comment("생성 시 IP")
    var createdIp: String? = null,

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Comment("수정한 사용자 ID")
    var updatedBy: Long? = null,

    @Column(length = 45)
    @Comment("수정 시 IP")
    var updatedIp: String? = null
)