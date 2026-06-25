package com.github.s8u.streamarchive.channel.usecase.dto.command

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType

data class ChannelAdminQuickCreateCommand(
    val name: String,
    val contentPrivacy: ChannelContentPrivacy,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean,
    val schedule: ScheduleCommand?
) {

    data class ScheduleCommand(
        val scheduleType: RecordScheduleType,
        val value: String,
        val recordQuality: RecordQuality,
        val priority: Int,
        val autoArchive: Boolean
    )
}
