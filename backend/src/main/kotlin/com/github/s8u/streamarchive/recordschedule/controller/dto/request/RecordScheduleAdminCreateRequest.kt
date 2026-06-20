package com.github.s8u.streamarchive.recordschedule.controller.dto.request

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminCreateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "녹화 스케줄 생성 요청 (관리자)")
data class RecordScheduleAdminCreateRequest(
    @field:Schema(description = "채널 ID", example = "1")
    val channelId: Long,

    @field:Schema(description = "플랫폼 유형 (CHZZK/SOOP/TWITCH/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "녹화 스케줄 유형 (ONCE/ALWAYS/WEEKLY/SPECIFIC)")
    val scheduleType: RecordScheduleType,

    @field:Schema(description = "스케줄 값")
    val value: String,

    @field:Schema(description = "녹화 화질")
    val recordQuality: RecordQuality = RecordQuality.BEST,

    @field:Schema(description = "우선순위", example = "0")
    val priority: Int = 0
) {

    fun toCommand(): RecordScheduleAdminCreateCommand {
        return RecordScheduleAdminCreateCommand(
            channelId = channelId,
            platformType = platformType,
            scheduleType = scheduleType,
            value = value,
            recordQuality = recordQuality,
            priority = priority
        )
    }
}
