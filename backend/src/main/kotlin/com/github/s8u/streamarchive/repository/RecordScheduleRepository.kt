package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordScheduleType
import org.springframework.data.jpa.repository.JpaRepository

interface RecordScheduleRepository : JpaRepository<RecordSchedule, Long>, RecordScheduleRepositoryCustom {
    fun findByChannel(channel: Channel): List<RecordSchedule>
    fun findByChannelAndPlatformType(
        channel: Channel,
        platformType: PlatformType
    ): List<RecordSchedule>
    fun existsByChannelAndPlatformTypeAndScheduleType(
        channel: Channel,
        platformType: PlatformType,
        scheduleType: RecordScheduleType
    ): Boolean
}