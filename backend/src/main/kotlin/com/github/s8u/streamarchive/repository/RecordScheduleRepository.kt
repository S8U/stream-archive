package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordScheduleType
import org.springframework.data.jpa.repository.JpaRepository

interface RecordScheduleRepository : JpaRepository<RecordSchedule, Long> {
    fun existsByChannelIdAndPlatformTypeAndScheduleTypeAndIsActive(
        channelId: Long,
        platformType: PlatformType,
        scheduleType: RecordScheduleType,
        isActive: Boolean
    ): Boolean
}