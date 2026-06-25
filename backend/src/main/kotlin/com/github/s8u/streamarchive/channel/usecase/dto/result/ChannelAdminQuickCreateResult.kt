package com.github.s8u.streamarchive.channel.usecase.dto.result

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import java.time.LocalDateTime

/**
 * 간편 채널 생성 결과 (관리자)
 *
 * 한 번에 만들어진 채널·플랫폼 연동·녹화 스케줄을 함께 담는다.
 */
data class ChannelAdminQuickCreateResult(
    val id: Long,
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val contentPrivacy: ChannelContentPrivacy,
    val platform: PlatformInfo,
    val schedule: ScheduleInfo?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    data class PlatformInfo(
        val id: Long,
        val platformType: PlatformType,
        val platformChannelId: String,
        val platformUrl: String,
        val isSyncProfile: Boolean
    )

    data class ScheduleInfo(
        val id: Long,
        val scheduleType: RecordScheduleType,
        val value: String,
        val recordQuality: RecordQuality,
        val priority: Int,
        val autoArchive: Boolean
    )

    companion object {
        fun from(
            channel: Channel,
            channelPlatform: ChannelPlatform,
            recordSchedule: RecordSchedule?,
            profileUrl: String,
            platformUrl: String
        ): ChannelAdminQuickCreateResult {
            return ChannelAdminQuickCreateResult(
                id = channel.id!!,
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                contentPrivacy = channel.contentPrivacy,
                platform = PlatformInfo(
                    id = channelPlatform.id!!,
                    platformType = channelPlatform.platformType,
                    platformChannelId = channelPlatform.platformChannelId,
                    platformUrl = platformUrl,
                    isSyncProfile = channelPlatform.isSyncProfile
                ),
                schedule = recordSchedule?.let {
                    ScheduleInfo(
                        id = it.id!!,
                        scheduleType = it.scheduleType,
                        value = it.value,
                        recordQuality = it.recordQuality,
                        priority = it.priority,
                        autoArchive = it.autoArchive
                    )
                },
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}
