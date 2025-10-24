package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordQuality
import com.github.s8u.streamarchive.enums.RecordScheduleType
import java.time.LocalDateTime

data class RecordScheduleCreateRequest(
    val channelId: Long,
    val platformType: PlatformType,
    val scheduleType: RecordScheduleType,
    val value: String,
    val recordQuality: RecordQuality = RecordQuality.BEST,
    val priority: Int = 0
)

data class RecordScheduleUpdateRequest(
    val platformType: PlatformType?,
    val scheduleType: RecordScheduleType?,
    val value: String?,
    val recordQuality: RecordQuality?,
    val priority: Int?,
    val isActive: Boolean?
)

data class RecordScheduleResponse(
    val id: Long,
    val channelId: Long,
    val platformType: PlatformType,
    val scheduleType: RecordScheduleType,
    val value: String,
    val recordQuality: RecordQuality,
    val priority: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(recordSchedule: RecordSchedule): RecordScheduleResponse {
            return RecordScheduleResponse(
                id = recordSchedule.id!!,
                channelId = recordSchedule.channelId,
                platformType = recordSchedule.platformType,
                scheduleType = recordSchedule.scheduleType,
                value = recordSchedule.value,
                recordQuality = recordSchedule.recordQuality,
                priority = recordSchedule.priority,
                isActive = recordSchedule.isActive,
                createdAt = recordSchedule.createdAt,
                updatedAt = recordSchedule.updatedAt
            )
        }
    }
}
