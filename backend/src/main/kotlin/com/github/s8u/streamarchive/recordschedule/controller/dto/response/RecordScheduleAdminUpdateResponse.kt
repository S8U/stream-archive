package com.github.s8u.streamarchive.recordschedule.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.usecase.dto.result.RecordScheduleAdminUpdateResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "녹화 스케줄 수정 응답 (관리자)")
data class RecordScheduleAdminUpdateResponse(
    @field:Schema(description = "녹화 스케줄 ID", example = "1")
    val id: Long,

    @field:Schema(description = "채널 정보")
    val channel: ChannelInfo,

    @field:Schema(description = "플랫폼 유형 (CHZZK/SOOP/TWITCH/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "녹화 스케줄 유형 (ONCE/ALWAYS/WEEKLY/SPECIFIC)")
    val scheduleType: RecordScheduleType,

    @field:Schema(description = "스케줄 값")
    val value: String,

    @field:Schema(description = "녹화 화질")
    val recordQuality: RecordQuality,

    @field:Schema(description = "우선순위", example = "0")
    val priority: Int,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    data class ChannelInfo(
        val id: Long,
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    companion object {
        fun from(result: RecordScheduleAdminUpdateResult): RecordScheduleAdminUpdateResponse {
            return RecordScheduleAdminUpdateResponse(
                id = result.id,
                channel = ChannelInfo(
                    id = result.channel.id,
                    uuid = result.channel.uuid,
                    name = result.channel.name,
                    profileUrl = result.channel.profileUrl
                ),
                platformType = result.platformType,
                scheduleType = result.scheduleType,
                value = result.value,
                recordQuality = result.recordQuality,
                priority = result.priority,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
