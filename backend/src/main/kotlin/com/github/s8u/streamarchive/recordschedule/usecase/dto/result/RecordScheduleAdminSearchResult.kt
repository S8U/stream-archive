package com.github.s8u.streamarchive.recordschedule.usecase.dto.result

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import java.time.LocalDateTime

/**
 * 녹화 스케줄 목록 조회 결과 (관리자)
 */
data class RecordScheduleAdminSearchResult(
    val id: Long,
    val channel: ChannelInfo,
    val platformType: PlatformType,
    val scheduleType: RecordScheduleType,
    val value: String,
    val recordQuality: RecordQuality,
    val priority: Int,
    val autoArchive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    data class ChannelInfo(
        val id: Long,
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    companion object {
        fun from(
            recordSchedule: RecordSchedule,
            channelProfileUrl: String
        ): RecordScheduleAdminSearchResult {
            return RecordScheduleAdminSearchResult(
                id = recordSchedule.id!!,
                channel = ChannelInfo(
                    id = recordSchedule.channel.id!!,
                    uuid = recordSchedule.channel.uuid,
                    name = recordSchedule.channel.name,
                    profileUrl = channelProfileUrl
                ),
                platformType = recordSchedule.platformType,
                scheduleType = recordSchedule.scheduleType,
                value = recordSchedule.value,
                recordQuality = recordSchedule.recordQuality,
                priority = recordSchedule.priority,
                autoArchive = recordSchedule.autoArchive,
                createdAt = recordSchedule.createdAt,
                updatedAt = recordSchedule.updatedAt
            )
        }
    }
}
