package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordScheduleType
import org.springframework.data.jpa.repository.JpaRepository

interface RecordScheduleRepository : JpaRepository<RecordSchedule, Long>, RecordScheduleRepositoryCustom {
    fun findByChannelId(channelId: Long): List<RecordSchedule>
    fun findByChannelIdAndPlatformType(
        channelId: Long,
        platformType: PlatformType
    ): List<RecordSchedule>
    fun existsByChannelIdAndPlatformTypeAndScheduleType(
        channelId: Long,
        platformType: PlatformType,
        scheduleType: RecordScheduleType
    ): Boolean
}