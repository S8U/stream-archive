package com.github.s8u.streamarchive.recordschedule.controller.dto.request

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminSearchCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "녹화 스케줄 검색 요청 (관리자)")
data class RecordScheduleAdminSearchRequest(
    @field:Schema(description = "녹화 스케줄 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val channelName: String? = null,

    @field:Schema(description = "플랫폼 유형 (CHZZK/SOOP/TWITCH/YOUTUBE)")
    val platformType: PlatformType? = null,

    @field:Schema(description = "녹화 스케줄 유형 (ONCE/ALWAYS/WEEKLY/SPECIFIC)")
    val scheduleType: RecordScheduleType? = null,

    @field:Schema(description = "녹화 화질")
    val recordQuality: RecordQuality? = null
) {

    fun toCommand(): RecordScheduleAdminSearchCommand {
        return RecordScheduleAdminSearchCommand(
            id = id,
            channelName = channelName,
            platformType = platformType,
            scheduleType = scheduleType,
            recordQuality = recordQuality
        )
    }
}
