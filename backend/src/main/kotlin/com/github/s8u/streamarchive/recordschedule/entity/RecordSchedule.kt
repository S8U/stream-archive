package com.github.s8u.streamarchive.recordschedule.entity

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    name = "record_schedules",
    indexes = [
        Index(name = "idx_record_schedules_channel_platform", columnList = "channelId,platformType"),
        Index(name = "idx_record_schedules_type", columnList = "scheduleType"),
        Index(name = "idx_record_schedules_is_active", columnList = "isActive"),
        Index(name = "idx_record_schedules_priority", columnList = "priority")
    ]
)
@SQLRestriction("is_active = true")
@Comment("녹화 스케줄")
class RecordSchedule(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId", nullable = false)
    @Comment("채널")
    val channel: Channel,

    platformType: PlatformType,
    scheduleType: RecordScheduleType,
    value: String,
    recordQuality: RecordQuality = RecordQuality.BEST,
    priority: Int = 0
) : BaseSoftDeleteEntity() {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    var platformType: PlatformType = platformType
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("녹화 스케줄 유형")
    var scheduleType: RecordScheduleType = scheduleType
        protected set

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("스케줄 값")
    var value: String = value
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("녹화 화질")
    var recordQuality: RecordQuality = recordQuality
        protected set

    @Column(nullable = false)
    @Comment("우선순위")
    var priority: Int = priority
        protected set

    /**
     * 녹화 스케줄 정보를 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(
        platformType: PlatformType?,
        scheduleType: RecordScheduleType?,
        value: String?,
        recordQuality: RecordQuality?,
        priority: Int?
    ) {
        platformType?.let { this.platformType = it }
        scheduleType?.let { this.scheduleType = it }
        value?.let { this.value = it }
        recordQuality?.let { this.recordQuality = it }
        priority?.let { this.priority = it }
    }

}
