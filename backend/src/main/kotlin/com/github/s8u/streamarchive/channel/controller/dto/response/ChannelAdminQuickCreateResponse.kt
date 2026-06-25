package com.github.s8u.streamarchive.channel.controller.dto.response

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminQuickCreateResult
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "간편 채널 생성 응답 (관리자)")
data class ChannelAdminQuickCreateResponse(
    @field:Schema(description = "채널 ID", example = "1")
    val id: Long,

    @field:Schema(description = "채널 UUID")
    val uuid: String,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String,

    @field:Schema(description = "프로필 이미지 URL")
    val profileUrl: String,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy,

    @field:Schema(description = "플랫폼 연동 정보")
    val platform: PlatformResponse,

    @field:Schema(description = "녹화 스케줄 정보 (없으면 null)")
    val schedule: ScheduleResponse?,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    @Schema(description = "플랫폼 연동 정보")
    data class PlatformResponse(
        @field:Schema(description = "플랫폼 연동 ID", example = "1")
        val id: Long,

        @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
        val platformType: PlatformType,

        @field:Schema(description = "플랫폼 채널 ID")
        val platformChannelId: String,

        @field:Schema(description = "플랫폼 URL")
        val platformUrl: String,

        @field:Schema(description = "프로필 동기화 여부", example = "true")
        val isSyncProfile: Boolean
    )

    @Schema(description = "녹화 스케줄 정보")
    data class ScheduleResponse(
        @field:Schema(description = "스케줄 ID", example = "1")
        val id: Long,

        @field:Schema(description = "녹화 스케줄 유형 (ONCE/ALWAYS/N_DAYS_OF_EVERY_WEEK/SPECIFIC_DAY)")
        val scheduleType: RecordScheduleType,

        @field:Schema(description = "스케줄 값")
        val value: String,

        @field:Schema(description = "녹화 화질")
        val recordQuality: RecordQuality,

        @field:Schema(description = "우선순위", example = "0")
        val priority: Int,

        @field:Schema(description = "자동 소장 여부", example = "false")
        val autoArchive: Boolean
    )

    companion object {
        fun from(result: ChannelAdminQuickCreateResult): ChannelAdminQuickCreateResponse {
            return ChannelAdminQuickCreateResponse(
                id = result.id,
                uuid = result.uuid,
                name = result.name,
                profileUrl = result.profileUrl,
                contentPrivacy = result.contentPrivacy,
                platform = PlatformResponse(
                    id = result.platform.id,
                    platformType = result.platform.platformType,
                    platformChannelId = result.platform.platformChannelId,
                    platformUrl = result.platform.platformUrl,
                    isSyncProfile = result.platform.isSyncProfile
                ),
                schedule = result.schedule?.let {
                    ScheduleResponse(
                        id = it.id,
                        scheduleType = it.scheduleType,
                        value = it.value,
                        recordQuality = it.recordQuality,
                        priority = it.priority,
                        autoArchive = it.autoArchive
                    )
                },
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
