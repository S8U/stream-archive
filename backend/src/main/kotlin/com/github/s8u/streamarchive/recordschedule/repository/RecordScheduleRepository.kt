package com.github.s8u.streamarchive.recordschedule.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import org.springframework.data.jpa.repository.JpaRepository

interface RecordScheduleRepository : JpaRepository<RecordSchedule, Long>, RecordScheduleRepositoryCustom {

    fun findByChannel(channel: Channel): List<RecordSchedule>
    fun findByChannelAndPlatformType(
        channel: Channel,
        platformType: PlatformType
    ): List<RecordSchedule>
    fun findByChannelIdAndPlatformType(
        channelId: Long,
        platformType: PlatformType
    ): List<RecordSchedule>
    fun existsByChannelAndPlatformTypeAndScheduleType(
        channel: Channel,
        platformType: PlatformType,
        scheduleType: RecordScheduleType
    ): Boolean

}
