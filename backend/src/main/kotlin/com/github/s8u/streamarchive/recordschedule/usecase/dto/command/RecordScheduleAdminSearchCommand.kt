package com.github.s8u.streamarchive.recordschedule.usecase.dto.command

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType

data class RecordScheduleAdminSearchCommand(
    val id: Long? = null,
    val channelName: String? = null,
    val platformType: PlatformType? = null,
    val scheduleType: RecordScheduleType? = null,
    val recordQuality: RecordQuality? = null
)
