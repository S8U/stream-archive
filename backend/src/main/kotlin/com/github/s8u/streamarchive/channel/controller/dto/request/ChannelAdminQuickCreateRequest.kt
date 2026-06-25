package com.github.s8u.streamarchive.channel.controller.dto.request

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminQuickCreateCommand
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "간편 채널 생성 요청 (관리자)")
data class ChannelAdminQuickCreateRequest(
    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy,

    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String,

    @field:Schema(description = "프로필 동기화 여부", example = "true")
    val isSyncProfile: Boolean,

    @field:Schema(description = "녹화 스케줄 (생략하면 스케줄 없이 채널·플랫폼만 만든다)")
    val schedule: ScheduleRequest? = null
) {

    @Schema(description = "녹화 스케줄 요청")
    data class ScheduleRequest(
        @field:Schema(description = "녹화 스케줄 유형 (ONCE/ALWAYS/N_DAYS_OF_EVERY_WEEK/SPECIFIC_DAY)")
        val scheduleType: RecordScheduleType,

        @field:Schema(
            description = "스케줄 값 (요일·날짜 JSON 배열, ONCE/ALWAYS는 빈 값)",
            example = "[]"
        )
        val value: String,

        @field:Schema(description = "녹화 화질")
        val recordQuality: RecordQuality,

        @field:Schema(description = "우선순위", example = "0")
        val priority: Int,

        @field:Schema(description = "자동 소장 여부", example = "false")
        val autoArchive: Boolean
    )

    fun toCommand(): ChannelAdminQuickCreateCommand {
        return ChannelAdminQuickCreateCommand(
            name = name,
            contentPrivacy = contentPrivacy,
            platformType = platformType,
            platformChannelId = platformChannelId,
            isSyncProfile = isSyncProfile,
            schedule = schedule?.let {
                ChannelAdminQuickCreateCommand.ScheduleCommand(
                    scheduleType = it.scheduleType,
                    value = it.value,
                    recordQuality = it.recordQuality,
                    priority = it.priority,
                    autoArchive = it.autoArchive
                )
            }
        )
    }
}
