package com.github.s8u.streamarchive.recordschedule.usecase.dto.command

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType

data class RecordScheduleAdminUpdateCommand(
    val platformType: PlatformType?,
    val scheduleType: RecordScheduleType?,
    val value: String?,
    val recordQuality: RecordQuality?,
    val priority: Int?,
    val autoArchive: Boolean?
)
